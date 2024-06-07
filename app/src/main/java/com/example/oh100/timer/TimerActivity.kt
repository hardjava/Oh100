package com.example.oh100.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.icu.text.DecimalFormat
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.oh100.R
import com.example.oh100.databinding.TimePickerViewBinding
import com.example.oh100.databinding.TimerViewBinding
import com.example.oh100.solved.Problem
import com.example.oh100.solved.TierImage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

// TODO : Cloud Firestore와 연계해서 오늘 푼 문제 수를 친구 리스트 및 마이페이지에 표시하도록 설정

class TimerActivity : AppCompatActivity() {
    private lateinit var binding : TimerViewBinding
    private val df = DecimalFormat("00")
    private var job: Job? = null
    private val channel = Channel<String>()
    private var update_job: Job? = null

    private lateinit var sharedPreferences: SharedPreferences
    private val pefs_file_name = "timer_prefs"
    private val timer_h = "timer_h"
    private val timer_m = "timer_m"
    private val timer_s = "timer_s"
    private val timer_is_running = "timer_is_running"
    private val timer_solving_time = "timer_solving_time"
    private val timer_problem_number = "timer_problem_number"

    private var h = 0
    private var m = 0
    private var s = 0
    private var is_running = false
    private var solving_time = 0
    private var problem_number : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = TimerViewBinding.inflate(layoutInflater)
        setContentView(binding.timerLayout)

        sharedPreferences = getSharedPreferences(pefs_file_name, Context.MODE_PRIVATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timer_Channel"
            val descriptionText = "Notification Channel for Timer"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val timer_channel = NotificationChannel("Timer", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(timer_channel)
        }

        binding.twoHoursButton.setOnClickListener {
            job?.cancel()

            is_running = false

            h = 2
            m = 0
            s = 0

            binding.timerText.text = "02:00:00"
        }

        binding.oneHourButton.setOnClickListener {
            job?.cancel()

            is_running = false

            h = 1
            m = 0
            s = 0

            binding.timerText.text = "01:00:00"
        }

        binding.thirtyMinutesButton.setOnClickListener {
            job?.cancel()

            is_running = false

            h = 0
            m = 30
            s = 0

            binding.timerText.text = "00:30:00"
        }

        binding.setButton.setOnClickListener {
            job?.cancel()

            is_running = false

            val builder = AlertDialog.Builder(this)

            val dialog_binding = TimePickerViewBinding.inflate(layoutInflater)

            builder.setView(dialog_binding.root)

            dialog_binding.hourPicker.minValue = 0
            dialog_binding.hourPicker.maxValue = 99
            dialog_binding.minutePicker.minValue = 0
            dialog_binding.minutePicker.maxValue = 59
            dialog_binding.secondPicker.minValue = 0
            dialog_binding.secondPicker.maxValue = 59

            builder.setPositiveButton("set") { dialog, _ ->
                h = dialog_binding.hourPicker.value
                m = dialog_binding.minutePicker.value
                s = dialog_binding.secondPicker.value

                binding.timerText.text = "${df.format(h)}:${df.format(m)}:${df.format(s)}"

                val temp_number = dialog_binding.timerProblemNumber.text.toString()

                if(!temp_number.isBlank() && temp_number.all { it.isDigit() }) {
                    problem_number = temp_number.toInt()

                    CoroutineScope(Dispatchers.Main).launch {
                        val problem = Problem()
                        problem.init(problem_number!!)

                        if (problem.getTitle() != null) {
                            TierImage.load(this@TimerActivity, binding.problemImage, problem.getLevel())
                            binding.problemImage.visibility = View.VISIBLE

                            binding.problemText.text =
                                "Problem ${problem_number} : ${problem.getTitle()}"
                            binding.problemText.visibility = View.VISIBLE
                        } else {
                            Toast.makeText(this@TimerActivity, "Problem doesn't exist", Toast.LENGTH_SHORT).show()

                            binding.problemImage.visibility = View.INVISIBLE
                            binding.problemText.visibility = View.INVISIBLE
                        }
                    }
                }

                dialog.dismiss()
            }
            builder.setNegativeButton("cancel") { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }

        binding.startButton.setOnClickListener {
            if(!is_running && !(s == 0 && m == 0 && h == 0)) {
                is_running = true

                solving_time = 0

                job?.cancel()
                job = CoroutineScope(Dispatchers.Default).launch {
                    while (is_running) {
                        delay(1000)

                        if(s == 0 && m == 0 && h == 0) {
                            is_running = false

                            val builder = NotificationCompat.Builder(this@TimerActivity, "Timer")
                                .setSmallIcon(R.drawable.ic_notification) // 알림 아이콘 설정
                                .setContentTitle("Time's up!")
                                .setContentText("Problem solving time is over.")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                            with(NotificationManagerCompat.from(this@TimerActivity)) {
                                notify(100, builder.build())
                            }

                            break
                        }

                        s--
                        if (s < 0) {
                            s = 59
                            m--
                        }
                        if (m < 0) {
                            m = 59
                            h--
                        }
                        if (h < 0) {
                            h = 0
                        }

                        solving_time++

                        val time = "${df.format(h)}:${df.format(m)}:${df.format(s)}"
                        channel.send(time)
                    }
                }
            }
        }

        binding.pauseButton.setOnClickListener {
            if(is_running)
                job?.cancel()

            is_running = false
        }

        binding.solvedButton.setOnClickListener {
            if(is_running)
                job?.cancel()

            is_running = false

            h = 0
            m = 0
            s = 0

            binding.timerText.text = "00:00:00"

            val solved_hour = solving_time / 3600;
            val solved_minute = (solving_time % 3600) / 60;
            val solved_second = solving_time % 60;

            AlertDialog.Builder(this)
                .setMessage(binding.problemText.text.toString() + " is solved in $solved_hour:$solved_minute:$solved_second.")
                .setPositiveButton("close") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        update_job = GlobalScope.launch(Dispatchers.Main) {
            channel.consumeEach {
                binding.timerText.text = "$it"
            }
        }
    }

    override fun onPause() {
        super.onPause()

        job?.cancel()

        with(sharedPreferences.edit()) {
            putInt(timer_h, h)
            putInt(timer_m, m)
            putInt(timer_s, s)
            putBoolean(timer_is_running, is_running)
            putInt(timer_solving_time, solving_time)
            if(problem_number != null)
                putInt(timer_problem_number, problem_number!!)
            apply()
        }
    }

    override fun onResume() {
        super.onResume()

        h = sharedPreferences.getInt(timer_h, 0)
        m = sharedPreferences.getInt(timer_m, 0)
        s = sharedPreferences.getInt(timer_s, 0)
        is_running = sharedPreferences.getBoolean(timer_is_running, false)
        solving_time = sharedPreferences.getInt(timer_solving_time, 0)
        problem_number = sharedPreferences.getInt(timer_problem_number, 0)

        binding.timerText.text = "${df.format(h)}:${df.format(m)}:${df.format(s)}"

        CoroutineScope(Dispatchers.Main).launch {
            val problem = Problem()
            problem.init(problem_number!!)

            if (problem.getTitle() != null) {
                TierImage.load(this@TimerActivity, binding.problemImage, problem.getLevel())
                binding.problemImage.visibility = View.VISIBLE

                binding.problemText.text =
                    "Problem ${problem_number} : ${problem.getTitle()}"
                binding.problemText.visibility = View.VISIBLE
            } else {
                Toast.makeText(this@TimerActivity, "Problem doesn't exist", Toast.LENGTH_SHORT).show()

                binding.problemImage.visibility = View.INVISIBLE
                binding.problemText.visibility = View.INVISIBLE
            }
        }

        if(is_running && !(s == 0 && m == 0 && h == 0)) {
            job?.cancel()
            job = CoroutineScope(Dispatchers.Default).launch {
                while (is_running) {
                    delay(1000)

                    if(s == 0 && m == 0 && h == 0) {
                        is_running = false

                        val builder = NotificationCompat.Builder(this@TimerActivity, "Timer")
                            .setSmallIcon(R.drawable.ic_notification) // 알림 아이콘 설정
                            .setContentTitle("Time's up!")
                            .setContentText("Problem solving time is over.")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                        with(NotificationManagerCompat.from(this@TimerActivity)) {
                            notify(100, builder.build())
                        }

                        break
                    }

                    s--
                    if (s < 0) {
                        s = 59
                        m--
                    }
                    if (m < 0) {
                        m = 59
                        h--
                    }
                    if (h < 0) {
                        h = 0
                    }

                    solving_time++

                    val time = "${df.format(h)}:${df.format(m)}:${df.format(s)}"
                    channel.send(time)
                }
            }
        }
    }

    override fun onDestroy() {
        job?.cancel()
        update_job?.cancel()

        super.onDestroy()
    }
}
