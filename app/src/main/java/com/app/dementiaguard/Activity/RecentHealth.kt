package com.app.dementiaguard.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.dementiaguard.Components.ProgressLoader.ProgressLoader
import com.app.dementiaguard.Model.Dto.MonthlySensorDataDto
import com.app.dementiaguard.Model.Response.ApiRes
import com.app.dementiaguard.R
import com.app.dementiaguard.Service.RetrofitService
import com.app.dementiaguard.Service.SensorDataService
import com.app.dementiaguard.Utils.StatusBarUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import java.text.DecimalFormat

class RecentHealth : AppCompatActivity() {
    ;
    private lateinit var tvRAStatsDateMainVal: TextView

    private lateinit var tvRAPulseRateMainVal: TextView
    private lateinit var tvRABloodOxygenMainVal: TextView
    private lateinit var tvRABloodCelciusMainVal: TextView
    private lateinit var tvRABloodStepsMainVal: TextView

    private lateinit var tvRAMaxPulseRateVal: TextView
    private lateinit var tvRAMinPulseRateVal: TextView
    private lateinit var tvRAAvgPulseRateVal: TextView

    private lateinit var tvRAMaxBodyTempVal: TextView
    private lateinit var tvRAMinBodyTempVal: TextView
    private lateinit var tvRAAvgBodyTempVal: TextView

    private lateinit var tvRAMaxBloodOxygenVal: TextView
    private lateinit var tvRAMinBloodOxygenVal: TextView
    private lateinit var tvRAAvgBloodOxygenVal: TextView

    private lateinit var svRALayout:ScrollView

    private lateinit var progressLoader: ProgressLoader


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recent_health)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Exclude bottom padding
            insets
        }
        StatusBarUtil.setStatusBarAppearance(this, true)

        bindViews()
        fetchData()
    }
    private fun bindViews() {
        progressLoader = ProgressLoader(this,"Crunching Data","Please Wait")
//        progressLoader.startProgressLoader()

        tvRAStatsDateMainVal = findViewById(R.id.tvRAStatsDateMainVal)

        tvRAPulseRateMainVal = findViewById(R.id.tvRAPulseRateMainVal)
        tvRABloodOxygenMainVal = findViewById(R.id.tvRABloodOxygenMainVal)
        tvRABloodCelciusMainVal = findViewById(R.id.tvRABloodCelciusMainVal)
        tvRABloodStepsMainVal = findViewById(R.id.tvRABloodStepsMainVal)

        tvRAMaxPulseRateVal = findViewById(R.id.tvRAMaxPulseRateVal)
        tvRAMinPulseRateVal = findViewById(R.id.tvRAMinPulseRateVal)
        tvRAAvgPulseRateVal = findViewById(R.id.tvRAAvgPulseRateVal)

        tvRAMaxBodyTempVal = findViewById(R.id.tvRAMaxBodyTempVal)
        tvRAMinBodyTempVal = findViewById(R.id.tvRAMinBodyTempVal)
        tvRAAvgBodyTempVal = findViewById(R.id.tvRAAvgBodyTempVal)

        tvRAMaxBloodOxygenVal = findViewById(R.id.tvRAMaxBloodOxygenVal)
        tvRAMinBloodOxygenVal = findViewById(R.id.tvRAMinBloodOxygenVal)
        tvRAAvgBloodOxygenVal = findViewById(R.id.tvRAAvgBloodOxygenVal)

        svRALayout = findViewById(R.id.svRALayout)
    }

    private fun fetchData() {
        progressLoader.startProgressLoader()
        hideDataViews()

        val retrofitService = RetrofitService()
        val sensorDataService = retrofitService.getRetrofit().create(SensorDataService::class.java)

        val call: Call<ApiRes<List<MonthlySensorDataDto>>> = sensorDataService.getMonthlySensorData()
        call.enqueue(object : Callback<ApiRes<List<MonthlySensorDataDto>>> {
            override fun onResponse(
                call: Call<ApiRes<List<MonthlySensorDataDto>>>,
                response: Response<ApiRes<List<MonthlySensorDataDto>>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val apiResponse = response.body()!!
                    println("pko"+apiResponse.status)
                    if (apiResponse.status) {
                        val dataList = apiResponse.data

                        if (dataList.isNotEmpty()) {
                            val latestData = dataList.last()
                            populateData(latestData)
                            showDataViews()
                        } else {
                            Toast.makeText(this@RecentHealth, "No sensor data available", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        val message = apiResponse.message ?: "Unknown error occurred"
                        Toast.makeText(this@RecentHealth, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RecentHealth, "Failed to load data", Toast.LENGTH_SHORT).show()
                }

                progressLoader.dismissProgressLoader()
            }

            override fun onFailure(call: Call<ApiRes<List<MonthlySensorDataDto>>>, t: Throwable) {
                Log.e("RecentHealth", "Request Failed", t)
                Toast.makeText(this@RecentHealth, "Server Error", Toast.LENGTH_SHORT).show()
                progressLoader.dismissProgressLoader()
            }
        })
    }

    private fun populateData(sensor: MonthlySensorDataDto) {
        val df = DecimalFormat("0.00")

//        tvRAStatsDateMainVal.text = sensor.get_id()?.monthStart.toString()

        tvRAPulseRateMainVal.text = df.format(sensor.pulse_rate_avg)
        tvRABloodOxygenMainVal.text = df.format(sensor.blood_oxygen_avg)
        tvRABloodCelciusMainVal.text = "${df.format(sensor.temperature_avg)} °C"
        tvRABloodStepsMainVal.text = formatLargeNumber(sensor.step_count_sum.toInt())

        tvRAMaxPulseRateVal.text = "${sensor.pulse_rate_max} bpm"
        tvRAMinPulseRateVal.text = "${sensor.pulse_rate_min} bpm"
        tvRAAvgPulseRateVal.text = "${df.format(sensor.pulse_rate_avg)} bpm"

        tvRAMaxBodyTempVal.text = "${sensor.temperature_max} °C"
        tvRAMinBodyTempVal.text = "${sensor.temperature_min} °C"
        tvRAAvgBodyTempVal.text = "${df.format(sensor.temperature_avg)} °C"

        tvRAMaxBloodOxygenVal.text = "${sensor.blood_oxygen_max} %"
        tvRAMinBloodOxygenVal.text = "${sensor.blood_oxygen_min} %"
        tvRAAvgBloodOxygenVal.text = "${df.format(sensor.blood_oxygen_avg)} %"
    }

    private fun formatLargeNumber(number: Int): String {
        return when {
            number >= 1_000_000 -> String.format(Locale.US, "%.2fM", number / 1_000_000.0)
            number >= 1_000 -> String.format(Locale.US, "%.2fK", number / 1_000.0)
            else -> number.toString()
        }
    }

    private fun hideDataViews() {
        findViewById<View>(R.id.svRALayout).visibility = View.GONE
    }

    private fun showDataViews() {
        findViewById<View>(R.id.svRALayout).visibility = View.VISIBLE
    }
}