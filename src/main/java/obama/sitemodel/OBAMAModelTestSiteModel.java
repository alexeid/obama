package obama.sitemodel;

import java.util.Arrays;

import org.apache.commons.statistics.distribution.GammaDistribution;

import beast.base.core.Citation;
import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.inference.util.InputUtil;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.domain.PositiveReal;
import beast.base.spec.domain.UnitInterval;
import beast.base.spec.evolution.sitemodel.SiteModel;
import beast.base.spec.inference.parameter.RealScalarParam;
import beast.base.spec.type.IntScalar;
import beast.base.evolution.tree.Node;

@Description("Site model that jumps between with and without gamma sites, as well as with and without invariant sites")
@Citation(value="Remco Bouckaert. OBAMA: OBAMA for Bayesian amino-acid model averaging. Peerj. 2020.", year=2020, DOI="https://doi.org/10.7717/peerj.44129")
public class OBAMAModelTestSiteModel extends SiteModel {

	public Input<IntScalar<? extends NonNegativeInt>> hasGammaRatesInput = new Input<>("hasGammaRates", "flag indicating whether gamma rate heterogeneity should be used (if 1) or not (if 0)", Validate.REQUIRED);
	public Input<IntScalar<? extends NonNegativeInt>> hasInvariantSitesInput = new Input<>("hasInvariantSites", "flag indicating whether invariant sites should be used (if 1) or not (if 0)", Validate.REQUIRED);

	IntScalar<? extends NonNegativeInt> hasInvariantSites;
	IntScalar<? extends NonNegativeInt> hasGammaRates;

	@Override
	public void initAndValidate() {
		hasInvariantSites = hasInvariantSitesInput.get();
		hasGammaRates = hasGammaRatesInput.get();

		// validate before super.initAndValidate() because the parent's refresh() invokes
		// calculateCategoryRates() which dereferences shape/invar parameters
		if (shapeParameterInput.get() == null) {
			throw new IllegalArgumentException("shape parameter must be specified");
		}
		if (!(shapeParameterInput.get() instanceof RealScalarParam<PositiveReal> shape) || !shape.isEstimatedInput.get()) {
			throw new IllegalArgumentException("shape parameter must be a RealScalarParam with estimate=true");
		}
		if (invarParameterInput.get() == null) {
			throw new IllegalArgumentException("proportionInvariant parameter must be specified");
		}
		if (!(invarParameterInput.get() instanceof RealScalarParam<UnitInterval> invar) || !invar.isEstimatedInput.get()) {
			throw new IllegalArgumentException("proportionInvariant parameter must be a RealScalarParam with estimate=true");
		}

		super.initAndValidate();
	}

	@Override
    protected void calculateCategoryRates(final Node node) {
		Arrays.fill(categoryRates, 0.0);
		Arrays.fill(categoryProportions, 0.0);

		double propVariable = 1.0;
        int cat = 0;

        if (hasInvariantSites.get() > 0) {
            if (hasPropInvariantCategory) {
                categoryRates[0] = 0.0;
                categoryProportions[0] = invarParameter.get();
            }
            propVariable = 1.0 - invarParameter.get();
            if (hasPropInvariantCategory) {
                cat = 1;
            }
        } else {
            if (hasPropInvariantCategory) {
                categoryProportions[0] = 0.0;
                cat = 1;
            }
        }

        if (hasGammaRates.get() > 0) {

            final double a = shapeParameter.get();
            double mean = 0.0;
            final int gammaCatCount = categoryCount - cat;

            final GammaDistribution g = GammaDistribution.of(a, 1.0 / a);
            for (int i = 0; i < gammaCatCount; i++) {
                try {
                	if (useBeast1StyleGamma) {
                        categoryRates[i + cat] = GammaDistributionQuantile((2.0 * i + 1.0) / (2.0 * gammaCatCount), a, 1.0 / a);
                	} else {
                		categoryRates[i + cat] = g.inverseCumulativeProbability((2.0 * i + 1.0) / (2.0 * gammaCatCount));
                	}

                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Something went wrong with the gamma distribution calculation");
                    System.exit(-1);
                }
                mean += categoryRates[i + cat];

                categoryProportions[i + cat] = propVariable / gammaCatCount;
            }

            mean = (propVariable * mean) / gammaCatCount;

            for (int i = 0; i < gammaCatCount; i++) {
                categoryRates[i + cat] /= mean;
            }
        } else {
            categoryRates[cat] = 1.0 / propVariable;
            categoryProportions[cat] = propVariable;
        }

        ratesKnown = true;
    }


	@Override
	protected boolean requiresRecalculation() {
		boolean isDirty = false;
		if (InputUtil.isDirty(hasInvariantSitesInput) || InputUtil.isDirty(hasGammaRatesInput)) {
			isDirty = true;
            ratesKnown = false;
		}
		if (super.requiresRecalculation()) {
			isDirty = true;
		}
		return isDirty;
	}

	@Override
    public double getProportionInvariant() {
        if (hasInvariantSites.get() > 0) {
        	return invarParameter.get();
        } else {
        	return 0.0;
        }
    }

}
