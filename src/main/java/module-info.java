open module obama {
    requires beast.base;
    requires beast.pkgmgmt;
    requires bmodeltest;
    requires org.apache.commons.statistics.distribution;
    requires static beast.fx;
    requires static javafx.controls;
    requires static java.desktop;

    exports obama;
    exports obama.app.beauti;
    exports obama.likelihood;
    exports obama.operator;
    exports obama.sitemodel;
    exports obama.substitutionmodel;
    exports obama.substitutionmodel.mixture;

    provides beast.base.core.BEASTInterface with
        obama.OBAMAAnalyser,
        obama.PhyloHMM,
        obama.PhyloHMMAnalyser,
        obama.PhyloHMMLogger,
        obama.PhyloHMMSparse,
        obama.PhyloHMMVar,
        obama.RatesForConservedRegions,
        obama.Transition,
        obama.likelihood.MixedTreeLikelihood,
        obama.likelihood.MixtureTreeLikelihood,
        obama.operator.FreeRateOperator,
        obama.operator.MixedOperator,
        obama.sitemodel.C10MixedSiteModel,
        obama.sitemodel.C10SiteModel,
        obama.sitemodel.C20MixedSiteModel,
        obama.sitemodel.C20SiteModel,
        obama.sitemodel.C30MixedSiteModel,
        obama.sitemodel.C30SiteModel,
        obama.sitemodel.C40MixedSiteModel,
        obama.sitemodel.C40SiteModel,
        obama.sitemodel.C50MixedSiteModel,
        obama.sitemodel.C50SiteModel,
        obama.sitemodel.C60MixedSiteModel,
        obama.sitemodel.C60SiteModel,
        obama.sitemodel.MixedSiteModel,
        obama.sitemodel.MixtureSiteModel,
        obama.sitemodel.OBAMAModelTestSiteModel,
        obama.substitutionmodel.OBAMAComplexModel,
        obama.substitutionmodel.OBAMAModel,
        obama.substitutionmodel.OBAMA_AsymPfam,
        obama.substitutionmodel.OBAMA_Blosum62,
        obama.substitutionmodel.OBAMA_CpREV,
        obama.substitutionmodel.OBAMA_DCMut,
        obama.substitutionmodel.OBAMA_Dayhoff,
        obama.substitutionmodel.OBAMA_FLU,
        obama.substitutionmodel.OBAMA_HIVb,
        obama.substitutionmodel.OBAMA_HIVw,
        obama.substitutionmodel.OBAMA_JTT,
        obama.substitutionmodel.OBAMA_LG,
        obama.substitutionmodel.OBAMA_MtArt,
        obama.substitutionmodel.OBAMA_MtMam,
        obama.substitutionmodel.OBAMA_MtREV,
        obama.substitutionmodel.OBAMA_RtREV,
        obama.substitutionmodel.OBAMA_VT,
        obama.substitutionmodel.OBAMA_WAG,
        obama.substitutionmodel.PHAT,
        obama.substitutionmodel.ParameterisedAminoAcidModel,
        obama.substitutionmodel.mixture.M1,
        obama.substitutionmodel.mixture.M2,
        obama.substitutionmodel.mixture.M3,
        obama.substitutionmodel.mixture.M4;

    provides beastfx.app.inputeditor.InputEditor with
        obama.app.beauti.OBAMAModelTestInputEditor;
}
