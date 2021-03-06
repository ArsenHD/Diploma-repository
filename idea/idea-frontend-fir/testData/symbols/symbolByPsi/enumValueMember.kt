enum class Style(val value: String) {
    SHEET("foo") {
        override val exitAnimation: String
            get() = "bar"
    };

    abstract val exitAnimation: String
}

// RESULT
/*
KtFirValueParameterSymbol:
annotatedType: [] kotlin/String
annotationClassIds: []
annotations: []
callableIdIfNonLocal: null
hasDefaultValue: false
isExtension: false
isVararg: false
name: value
origin: SOURCE
receiverType: null
symbolKind: LOCAL

KtFirConstructorSymbol:
annotatedType: [] Style
annotationClassIds: []
annotations: []
callableIdIfNonLocal: null
containingClassIdIfNonLocal: Style
dispatchType: null
hasStableParameterNames: true
isExtension: false
isPrimary: true
origin: SOURCE
receiverType: null
symbolKind: MEMBER
typeParameters: []
valueParameters: [
    KtFirValueParameterSymbol(value)
  ]
visibility: Private

KtFirPropertyGetterSymbol:
annotatedType: [] kotlin/String
annotationClassIds: []
annotations: []
callableIdIfNonLocal: null
dispatchType: null
hasBody: true
isDefault: false
isExtension: false
isInline: false
isOverride: false
modality: FINAL
origin: SOURCE
receiverType: null
symbolKind: ACCESSOR
visibility: Public

KtFirKotlinPropertySymbol:
annotatedType: [] kotlin/String
annotationClassIds: []
annotations: []
callableIdIfNonLocal: null
dispatchType: <anonymous>
getter: KtFirPropertyGetterSymbol(<getter>)
hasBackingField: false
hasGetter: true
hasSetter: false
initializer: null
isConst: false
isExtension: false
isLateInit: false
isOverride: true
isStatic: false
isVal: true
modality: FINAL
name: exitAnimation
origin: SOURCE
receiverType: null
setter: null
symbolKind: MEMBER
visibility: Public

KtFirEnumEntrySymbol:
annotatedType: [] Style
callableIdIfNonLocal: /Style.SHEET
containingEnumClassIdIfNonLocal: Style
isExtension: false
name: SHEET
origin: SOURCE
receiverType: null
symbolKind: MEMBER

KtFirKotlinPropertySymbol:
annotatedType: [] kotlin/String
annotationClassIds: []
annotations: []
callableIdIfNonLocal: /Style.exitAnimation
dispatchType: Style
getter: KtFirPropertyGetterSymbol(<getter>)
hasBackingField: false
hasGetter: true
hasSetter: false
initializer: null
isConst: false
isExtension: false
isLateInit: false
isOverride: false
isStatic: false
isVal: true
modality: ABSTRACT
name: exitAnimation
origin: SOURCE
receiverType: null
setter: null
symbolKind: MEMBER
visibility: Public

KtFirNamedClassOrObjectSymbol:
annotationClassIds: []
annotations: []
classIdIfNonLocal: Style
classKind: ENUM_CLASS
companionObject: null
isData: false
isExternal: false
isFun: false
isInline: false
isInner: false
modality: FINAL
name: Style
origin: SOURCE
superTypes: [
    [] kotlin/Enum<Style>
  ]
symbolKind: TOP_LEVEL
typeParameters: []
visibility: Public
*/
