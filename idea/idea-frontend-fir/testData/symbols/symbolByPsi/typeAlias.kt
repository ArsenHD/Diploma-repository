class X<T>

typealias Y<Z> = X<Z>

// RESULT
/*
KtFirTypeParameterSymbol:
  isReified: false
  name: T
  origin: SOURCE
  upperBounds: [
    kotlin/Any?
  ]
  variance: INVARIANT

KtFirNamedClassOrObjectSymbol:
  annotationClassIds: []
  annotations: []
  classIdIfNonLocal: X
  classKind: CLASS
  companionObject: null
  isData: false
  isExternal: false
  isFun: false
  isInline: false
  isInner: false
  modality: FINAL
  name: X
  origin: SOURCE
  superTypes: [
    [] kotlin/Any
  ]
  symbolKind: TOP_LEVEL
  typeParameters: [
    KtFirTypeParameterSymbol(T)
  ]
  visibility: Public

KtFirTypeParameterSymbol:
  isReified: false
  name: Z
  origin: SOURCE
  upperBounds: [
    kotlin/Any?
  ]
  variance: INVARIANT

KtFirTypeAliasSymbol:
  classIdIfNonLocal: Y
  expandedType: X<Z>
  name: Y
  origin: SOURCE
  symbolKind: TOP_LEVEL
*/
