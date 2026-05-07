# OBAMA BEAST 3 strong-typing migration — what's left

The BEAST 3 strong-typing migration on the `beast3-migration` branch
covers parameter inputs, the OBAMA(Complex)Model / ParameterisedAminoAcidModel
parents, and the 16 OBAMA_<aa> empirical models. The remaining work is
the SiteModel layer (mirrors the bModelTest plan) plus the OBAMA
example XMLs.

## Still on legacy parents

| Class | Parent (legacy) | What it forces to legacy |
|---|---|---|
| `OBAMAModelTestSiteModel` | `beast.base.evolution.sitemodel.SiteModel` | `shape`, `proportionInvariant` are `RealParameter` |

This parallels `BEASTModelTestSiteModel` in the bModelTest repo —
fix them together. See `bModelTest/MIGRATION-NEXT.md` for the
detailed plan.

## OBAMA-specific clean-up

The OBAMA example XMLs (`examples/testOBAMA.xml`,
`src/main/resources/obama/fxtemplates/OBAMA.xml`, etc.) still declare
`OBAMA_frequencies` / `frequencies_OBAMA` as
`<parameter ... spec="parameter.RealParameter" dimension="20">…</parameter>`
and wrap the Dirichlet prior in a legacy `<prior name="distribution"
x="@…">`. With the spec `Frequencies` parent already in place
upstream of these, these XMLs should switch to:

- `<stateNode spec="SimplexParam">…</stateNode>` for the frequency vector
- a direct `beast.base.spec.inference.distribution.Dirichlet` with
  `param="@…"` (no `<prior>` wrapper)

See the equivalent change in `bModelTest/examples/bModelTest.xml` on
the `beast3-migration` branch for the pattern.

## Two test files still excluded

`src/test/java/test/obama/likelihood/SingleComponentTreeLikelihoodTest.java`
and `MixedTreeLikelihoodSingleComponentTest.java` are excluded in
`pom.xml` due to an upstream `beast.base` test-jar automatic-module-name
collision. Independent of this migration.

## References

- Migration guide: `~/Git/beast3/scripts/migration-guide.md`
- Spec README: `~/Git/beast3/beast-base/src/main/resources/beast/base/spec/README.md`
- Gold-standard migrated packages: `~/Git/morph-models/`, `~/Git/sampled-ancestors/`
