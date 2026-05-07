package obama.substitutionmodel;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import beast.base.core.Citation;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Log;
import beast.base.core.Input.Validate;
import beast.base.evolution.datatype.Aminoacid;
import beast.base.evolution.datatype.DataType;
import beast.base.spec.evolution.substitutionmodel.ComplexSubstitutionModel;
import beast.base.spec.evolution.substitutionmodel.EmpiricalSubstitutionModel;
import beast.base.evolution.tree.Node;
import beast.base.inference.util.InputUtil;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.inference.parameter.BoolScalarParam;
import beast.base.spec.type.BoolScalar;
import beast.base.spec.type.IntScalar;
@Description("Complex substitution model that can average over a number of amino acid substitution models " +
		"as well as switch between the model's frequencies and external frequencies (as for example " +
		"empirical frequencies informed by an alignment).")
@Citation(value="Remco Bouckaert. OBAMA: OBAMA for Bayesian amino-acid model averaging. PeerJ 8, e9460",
		year = 2020, firstAuthorSurname = "bouckaert", DOI="doi.org/10.7717/peerj.9460")
public class OBAMAComplexModel extends ComplexSubstitutionModel {
	
	final public Input<BoolScalar> useExternalFreqsInput = new Input<>("useExternalFreqs", "if false, use substitution model frequencies, "
			+ "otherwise use frequencies from frequencies input (e.g. empirical frequencies)", new BoolScalarParam(false));
	final public Input<List<EmpiricalSubstitutionModel>> substModelInput = new Input<>("model", "empicial amino acid substitution model", new ArrayList<>(), Validate.REQUIRED);
	final public Input<IntScalar<? extends NonNegativeInt>> modelIndicatorInput = new Input<>("modelIndicator", "index of the model in list of models that is used for its rates and frequencies", Validate.REQUIRED);

	BoolScalar useExternalFreqs;
	IntScalar<? extends NonNegativeInt> modelIndicator;
	List<EmpiricalSubstitutionModel> models;
	
	public OBAMAComplexModel() {
		ratesInput.setRule(Validate.OPTIONAL);
	}
	

	@Override
	public void initAndValidate() {
        frequencies = frequenciesInput.get();

		useExternalFreqs = useExternalFreqsInput.get();
		models = substModelInput.get();
		modelIndicator = modelIndicatorInput.get();
		// Range is enforced by the NonNegativeInt domain; no per-instance upper bound on IntScalarParam.
		if (modelIndicator.get() > models.size() - 1 || modelIndicator.get() < 0) {
			throw new IllegalArgumentException("modelIndicator value " + modelIndicator.get() +
					" is out of range [0, " + (models.size() - 1) + "]");
		}
		
		
        updateMatrix = true;
        nrOfStates = frequencies.getFreqs().length;

        try {
			eigenSystem = createEigenSystem();
		} catch (SecurityException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
        //eigenSystem = new DefaultEigenSystem(m_nStates);

        rateMatrix = new double[nrOfStates][nrOfStates];
        relativeRates = new double[nrOfStates*(nrOfStates-1)];
        storedRelativeRates = new double[nrOfStates*(nrOfStates-1)];

	}
	


	@Override
	public void setupRelativeRates() {
    	EmpiricalSubstitutionModel model = models.get(modelIndicator.get());
    	double [] empiricalRates = model.getEmpericalRateValues();
        System.arraycopy(empiricalRates, 0, relativeRates, 0, empiricalRates.length);
    }

	@Override
	public double[] getFrequencies() {
		if (useExternalFreqs.get()) {
			return super.getFrequencies();
		}
    	EmpiricalSubstitutionModel model = models.get(modelIndicator.get());
        return model.getFrequencies();
	}
	
	
    @Override
    public double[] getRateMatrix(Node node) {
    	EmpiricalSubstitutionModel model = models.get(modelIndicator.get());
        double[][] matrix = model.getEmpiricalRates();
        int states = matrix.length;
        double[] rates = new double[states * states];
        for (int i = 0; i < states; i++) {
            for (int j = i + 1; j < states; j++) {
                rates[i * states + j] = matrix[i][j];
                rates[j * states + i] = matrix[i][j];
            }
        }
        // determine diagonal
        for (int i = 0; i < states; i++) {
            double sum = 0;
            for (int j = i + 1; j < states; j++) {
                sum += rates[i * states + j];
            }
            rates[i * states + i] = -sum;
        }
        return rates;
    }	
	
	
	@Override
	public boolean canHandleDataType(DataType dataType) {
        return dataType instanceof Aminoacid;
	}
	
	@Override
	protected boolean requiresRecalculation() {
		if (InputUtil.isDirty(useExternalFreqsInput)) {
			updateMatrix = true;
			return true;
		}
		if (InputUtil.isDirty(modelIndicatorInput)) {
			updateMatrix = true;
			return true;
		}
		return super.requiresRecalculation();
	}

}
