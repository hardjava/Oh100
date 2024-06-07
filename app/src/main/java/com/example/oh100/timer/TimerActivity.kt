package com.example.oh100.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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

class TimerActivity : AppCompatActivity() {
    private lateinit var binding : TimerViewBinding
    private val df = DecimalFormat("00")
    private var job: Job? = null
    private val channel = Channel<String>()
    private var update_job: Job? = null

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

            solving_time = 0;

            binding.timerText.text = "02:00:00"
        }

        binding.oneHourButton.setOnClickListener {
            job?.cancel()

            is_running = false

            h = 1
            m = 0
            s = 0

            solving_time = 0;

            binding.timerText.text = "01:00:00"
        }

        binding.thirtyMinutesButton.setOnClickListener {
            job?.cancel()

            is_running = false

            h = 0
            m = 30
            s = 0

            solving_time = 0;

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

                solving_time = 0;

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

            var problem_name = binding.problemText.text.toString()

            if(binding.problemText.visibility == View.INVISIBLE)
                problem_name = "The Problem"

            binding.problemImage.visibility = View.INVISIBLE
            binding.problemText.visibility = View.INVISIBLE

            if(solving_time != 0) {
                val solved_hour = solving_time / 3600
                val solved_minute = (solving_time % 3600) / 60
                val solved_second = solving_time % 60

                AlertDialog.Builder(this)
                    .setMessage(problem_name + " is solved in ${df.format(solved_hour)}:${df.format(solved_minute)}:${df.format(solved_second)}.")
                    .setPositiveButton("close") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            solving_time = 0;
        }

        update_job = GlobalScope.launch(Dispatchers.Main) {
            channel.consumeEach {
                binding.timerText.text = "$it"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        job?.cancel()
        update_job?.cancel()
    }
}
