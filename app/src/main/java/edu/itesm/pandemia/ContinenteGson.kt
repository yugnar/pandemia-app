package edu.itesm.pandemia

data class ContinenteGson(
    var continent:String?,
    var continentInfo: ContinentInfo,
    var cases:Double?,
    var recovered: Double?
)

data class ContinentInfo(
    var lat:Double?,
    var long:Double?
)
