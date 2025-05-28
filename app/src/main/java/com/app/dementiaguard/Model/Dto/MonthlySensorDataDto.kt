package com.app.dementiaguard.Model.Dto

import java.io.Serializable

class MonthlySensorDataDto : Serializable {
    private var _id: IdDto? = null
    var accelerometer_variance: Double = 0.0
    var activity: String? = null
    var blood_oxygen_avg: Double = 0.0
    var blood_oxygen_max: Int = 0
    var blood_oxygen_min: Int = 0
    var gyroscope_variance: Double = 0.0
    var pulse_rate_avg: Double = 0.0
    var pulse_rate_max: Int = 0
    var pulse_rate_min: Int = 0
    var step_count_sum: Long = 0
    var temperature_avg: Double = 0.0
    var temperature_max: Int = 0
    var temperature_min: Int = 0

    // Inner class for _id
    class IdDto : Serializable {
        var dgWearId: String? = null
        var monthStart: String? = null
    }

    // Getters and Setters
    fun get_id(): IdDto? {
        return _id
    }

    fun set_id(_id: IdDto?) {
        this._id = _id
    }
}