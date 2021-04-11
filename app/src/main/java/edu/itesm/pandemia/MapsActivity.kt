package edu.itesm.pandemia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class Pais(var nombre: String,
                var latitude: Double,
                var longitude: Double,
                var casos: Double,
                var recuperados: Double,
                var muertes: Double,
                var tests: Double)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val url = "https://disease.sh/v3/covid-19/countries"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        cargaDatos() // este es con Volley
        getCountries() // estos son con Retrofit
        getContinents() // Retrofit

    }

    override fun onStart() {
        super.onStart()
        Log.i("Lifecycle event", "onStart custom message")
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        /*val mexico = LatLng(19.432608, -99.133209)
        mMap.addMarker(MarkerOptions().position(mexico).title("Mexico City"))
        mMap.addMarker(MarkerOptions().position(LatLng(18.432608, -99.133209)).title("Desconocido"))

        mMap.moveCamera(CameraUpdateFactory.newLatLng(mexico))*/
    }

    private val data = mutableListOf<Pais>()

    fun viewData(view: View){
        /*for (pais in data){
            mMap.addMarker(MarkerOptions().position(LatLng(pais.latitude, pais.longitude)).title(pais.nombre))
        }*/
        mMap.clear();
        for (pais in paisesGson){
            mMap.addMarker(MarkerOptions().position(LatLng(pais?.countryInfo.lat?:0.0, pais?.countryInfo.long?:0.0)).title(pais.nombre))
        }
    }

    fun cargaDatos(){
        val requestQueue = Volley.newRequestQueue(this)
        val peticion = JsonArrayRequest(Request.Method.GET, url, null, Response.Listener {
            val jsonArray = it
            for(i in 0 until jsonArray.length()){
                val pais = jsonArray.getJSONObject(i)
                val nombre = pais.getString("country")
                val countryInfoData = pais.getJSONObject("countryInfo")

                val latitude = countryInfoData.getDouble("lat")
                val longitude = countryInfoData.getDouble("long")
                val casos = pais.getDouble("cases")
                val recuperdos = pais.getDouble("recovered")

                val muertes = pais.getDouble("deaths")
                val tests = pais.getDouble("tests")

                val paisObject = Pais(nombre,latitude, longitude, casos, recuperdos, muertes, tests)
                data.add(paisObject)
            }
        }, Response.ErrorListener {

        })
        requestQueue.add(peticion)

    }

    private fun getRetrofit():Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://disease.sh/v3/covid-19/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private lateinit var paisesGson: ArrayList<PaisGson>

    private fun getCountries(){
        val callToService = getRetrofit().create(APIService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val responseFromService = callToService.getCountries()
            runOnUiThread {
                paisesGson = responseFromService.body() as ArrayList<PaisGson>
                /*if (responseFromService.isSuccessful) {
                    Toast.makeText(applicationContext, "Datos obtenidos", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "Error!", Toast.LENGTH_LONG).show()
                }*/
            }
        }
    }

    private lateinit var continentesGson: ArrayList<ContinenteGson>

    private fun getContinents(){
        val callToService = getRetrofit().create(APIContinentService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val responseFromService = callToService.getContinents()
            runOnUiThread {
                continentesGson = responseFromService.body() as ArrayList<ContinenteGson>
                if (responseFromService.isSuccessful) {
                    Toast.makeText(applicationContext, "Datos obtenidos", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(applicationContext, "Error!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun viewDeathData(view: View){
        //Top 10 países con más defunciones
        val sortedDeaths = data.sortedByDescending { it.muertes }
        //Log.i("verEsto","Sorted by deaths ascending: $sortedDeaths")
        mMap.clear();
        for (i in 0..9){
            mMap.addMarker(MarkerOptions().position(LatLng(sortedDeaths[i].latitude, sortedDeaths[i].longitude)).title(sortedDeaths[i].nombre).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        }
       }

    fun viewTestsData(view: View){
        //Top 10 países con más tests realizados
        val sortedTests = data.sortedByDescending { it.tests }
        //Log.i("verEsto","Sorted by deaths ascending: $sortedCases")
        mMap.clear();
        for (i in 0..9){
            mMap.addMarker(MarkerOptions().position(LatLng(sortedTests[i].latitude, sortedTests[i].longitude)).title(sortedTests[i].nombre).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
        }
    }

    fun viewCasesData(view: View){
        //Top 10 países con menos casos reportados
        val sortedCases = data.sortedBy { it.casos }
        //Log.i("verEsto","Sorted by deaths ascending: $sortedCases")
        mMap.clear();
        for (i in 0..9){
            mMap.addMarker(MarkerOptions().position(LatLng(sortedCases[i].latitude, sortedCases[i].longitude)).title(sortedCases[i].nombre).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
        }
    }

    fun viewByContinent(view: View){
        //View Data by continent provided by Retrofit
        mMap.clear();
        for (continente in continentesGson){
            mMap.addMarker(MarkerOptions().position(LatLng(continente?.continentInfo.lat?:0.0, continente?.continentInfo.long?:0.0)).title(continente.continent).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
    }

}