package edu.itesm.pandemia

import retrofit2.Response
import retrofit2.http.GET

interface APIContinentService {
    @GET("continents")
    suspend fun getContinents() : Response<List<ContinenteGson>>
}