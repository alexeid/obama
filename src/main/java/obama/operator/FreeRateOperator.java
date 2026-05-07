package obama.operator;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.core.Input.Validate;
import beast.base.spec.domain.NonNegativeReal;
import beast.base.spec.domain.PositiveReal;
import beast.base.spec.inference.parameter.RealVectorParam;
import beast.base.util.HeapSort;
import beast.base.util.Randomizer;

@Description("Generates proposals for free rate model that ensures weighted average rate remains 1")
public class FreeRateOperator extends Operator {
    public final Input<RealVectorParam<? extends NonNegativeReal>> weightInput =
            new Input<>("weights", "weights of the various categories, should sum to 1", Validate.REQUIRED);
    public final Input<RealVectorParam<? extends PositiveReal>> rateParameterInput =
            new Input<>("rates", "rates for each of the categories, will be weighted and normalised to 1", Validate.REQUIRED);
    public final Input<Double> deltaInput = new Input<Double>("delta", "Magnitude of change for two randomly picked values.", 1.0);


	@Override
	public void initAndValidate() {
	}

	@Override
	public double proposal() {
		double HR = 0;
		// enforce sum weight*rate = constant
		if (Randomizer.nextBoolean()) {
			// Weighted Delta Exchange on weights
			HR = doNormalisedDeltaExchange(weightInput.get(), rateParameterInput.get());
		} else {
			// Weighted Delta Exchange on weights
			HR = doDeltaExchange(rateParameterInput.get(), weightInput.get());
		}
		// sort();
		return HR;
	}


	// sort rates and associated frequencies so rates are ordered from lowest to highest
	private void sort() {
		double [] rates = rateParameterInput.get().getValues();
		int [] index = new int[rates.length];
		for (int i = 0; i < index.length; i++) {
			index[i] = i;
		}
		HeapSort.sort(rates, index);
		double [] weights = weightInput.get().getValues();

		for (int i = 0; i < index.length; i++) {
			rateParameterInput.get().set(i, rates[index[i]]);
			weightInput.get().set(i, weights[index[i]]);
		}
	}

	private double doNormalisedDeltaExchange(RealVectorParam<? extends NonNegativeReal> realparameter, RealVectorParam<? extends PositiveReal> rates) {
        final int dim = realparameter.size();
        if (dim <= 1) {
        	// it is impossible to select two distinct entries in this case, so there is nothing to propose
        	return 0.0;
        }

        final int dim1 = Randomizer.nextInt(dim);
        int dim2 = dim1;
        while (dim1 == dim2) {
            dim2 = Randomizer.nextInt(dim);
        }

        // operate on real parameter
        double scalar1 = realparameter.get(dim1);
        double scalar2 = realparameter.get(dim2);

        // exchange a random delta
        final double d = Randomizer.nextDouble() * deltaInput.get();
        scalar1 -= d;
        scalar2 += d;
        if (!realparameter.isValid(scalar1) || !realparameter.isValid(scalar2)) {
            return Double.NEGATIVE_INFINITY;
        } else {
            realparameter.set(dim1, scalar1);
            realparameter.set(dim2, scalar2);

            // normalise
        	double sum = 0;
        	for (int i = 0; i < dim; i++) {
        		sum += realparameter.get(i) * rates.get(i);
        	}
        	for (int i = 0; i < dim; i++) {
        		double newRate = rates.get(i) /sum;
        		if (!rates.isValid(newRate)) {
        			return Double.NEGATIVE_INFINITY;
        		}
      			rates.set(i, newRate);
         	}


        }
		return 0;
	}

	private double doDeltaExchange(RealVectorParam<? extends PositiveReal> realparameter, RealVectorParam<? extends NonNegativeReal> parameterWeights) {

        final int dim = realparameter.size();
        if (dim <= 1) {
        	// it is impossible to select two distinct entries in this case, so there is nothing to propose
        	return 0.0;
        }

        final int dim1 = Randomizer.nextInt(dim);
        int dim2 = dim1;
        while (dim1 == dim2) {
            dim2 = Randomizer.nextInt(dim);
        }

        // operate on real parameter
        double scalar1 = realparameter.get(dim1);
        double scalar2 = realparameter.get(dim2);

        // exchange a random delta
        final double d = Randomizer.nextDouble() * deltaInput.get();
        scalar1 -= d;
        scalar2 += d * parameterWeights.get(dim1) / parameterWeights.get(dim2);

        if (!realparameter.isValid(scalar1) || !realparameter.isValid(scalar2)) {
            return Double.NEGATIVE_INFINITY;
        } else {
            realparameter.set(dim1, scalar1);
            realparameter.set(dim2, scalar2);
        }
		return 0;
	}

}
