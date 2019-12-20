package com.example.smartparking;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Geocoder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class TimerActivity extends AppCompatActivity
        implements TimePickerDialog.OnTimeSetListener, TimePickerDialog.OnCancelListener,
        DatePickerDialog.OnDateSetListener {

    //Constants used
    private  final int BLUE = Color.parseColor("#FFFFFF"); //WhiteThough
    private  final int DKGRAY = Color.parseColor("#000000"); //Black
    private  final int ACTIONBAR_ELEVATION = 0;
    private  final String ACTIONBAR_COLOR = "#FF13465E"; //DARK BLUE
    private  final String ACTIONBAR_TITLE = "Alarm";
    private  final String TIMEPICKERTAG = "time picker";
    private  final String DATEPICKERTAG = "date picker";
    private  final long ONESECONDINTERVAL = 1000;

    //Shared pref Key info
    private  final String SHAREDPREF_LOCATION = "locationString";
    private  final String SHAREDPREF_NAME = "parkinginfosharedpref";
    private  final String SHAREDPREF_ISTIMERRUNNING = "isTimerRunningBoolean";
    private  final String SHAREDPREF_STARTTIMELONG = "startTimeLong";
    private  final String SHAREDPREF_ENDTIMELONG = "endTimeLong";
    private  final String SHAREDPREF_REMINDERTIMELONG = "reminderTimeLong";
    private  final String SHAREDPREF_REMINDERHOURINT = "reminderHourInt";
    private  final String SHAREDPREF_REMINDERMINUTEINT = "reminderMinuteInt";
    private  final String DEFAULT_LOCATION = "Melbourne";
    //Get the location info from Parent intent
    private  final String PARENT_INTENT_LOCATION = "location";


    //Time Variables with different type formats
    private Timestamp inputStartTimestamp;
    private Timestamp inputEndTimestamp;

    private int reminderHourOfDay;
    private int reminderMinute;
    private Date currentTimeMSDate;
    private Date inputStartTimeMSDate;
    private Date inputEndTimeMSDate;
    private Date inputStartDate;
    private Date inputEndDate;
    private long remainingTime;
    private long reminderTime;
    private Date reminderTimeDate;

    //Flags used to track the actions
    private Boolean startDateClicked;
    private Boolean endDateClicked;
    private Boolean startTimeClicked;
    private Boolean endTimeClicked;
    private Boolean reminderTimeClicked;
    private Boolean isTimerRunning=false;
    private Boolean timesareFine= true;
    private String location;

    //Widgets program variables
    private TextView timerDisplay;
    private TextView startTimeText;
    private TextView endTimeText;
    private TextView startDateText;
    private TextView endDateText;
    private TextView reminderMinutesText;
    private EditText locationInputText;
    private Button timerstartButton;
    private Button timerprofileButton;
    private CountDownTimer countDownTimer;
    private Calendar calendarTimeEnd;
    private ProgressBar progressBar;

    //User Input for Profile History
    //UserID, Location, Start Time, End Time
    private Profile parkevent;

    //Alarm Managers and Intents
    private AlarmManager alarmManager;
    private AlarmManager alarmManagerReminder;
    private PendingIntent pendingIntent;
    private PendingIntent pendingIntentReminder;


    //Set up the Environment when the Timer Activity Starts
    //Main Inputs are set in onResume Module

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //First do the Super onCreate
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timer);

        //Set the Action Bar compatible to alarm View Screen
        //Restrict the Timer Activity to Portrait View, Title setup
        //Color and fullscreen setup
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setTitle(ACTIONBAR_TITLE);
        getSupportActionBar().setIcon(R.drawable.ic_alarm_icon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(ACTIONBAR_ELEVATION);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(
                Color.parseColor(ACTIONBAR_COLOR)));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        //Even if it is first time, Check if the
        //Alarms is running already. If so use the
        //Time info, flag and reminder time from them
        getSavedSharedPreferences();

        //Widget click flags are reset
        //Don't set the Input times and Location as it might be
        //Received from the Shared Pref. If it is not they will
        //be set to default. So only clear the flags
        clearAllWidgetsClickFlag();

        //Get input arguments from the Caller Function
        getLocationFromParentCaller();
        //Get the input layout fields to the class variables
        getLayoutWidgetsInClassVariables();
        //Set Start Time input text onClick Listener
        setStartDateOnClickListener();

        //Set Start Time input text onClick Listener
        setEndDateOnClickListener();
        //Set Start Time input text onClick Listener
        setStartTimeOnClickListener();
        //Set End Time input text onClick Listener
        setEndTimeOnClickListener();
        //Set ReminderTime input text onClick Listener
        setReminderTimeOnClickListener();

        //Set the Timer Start Button Click Listener - Start The Time
        setTimerStartButtonListerner();
        //Set the Timer Profile Button Click Listener - Update the profile
        setTimerProfileButtonListerner();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume(){
        super.onResume();

        //All Alarm logic is run here wherever the Activity is entered
        //onCreate or onStart. It ends up here.

        //Get the saved Shared Preference
        //Validate when to get it. It might cause issues
        getSavedSharedPreferences();

        if(isTimerRunning == false) {

            //This scope is run if the Alarm is not running
            //The activity is running for the first time
            //Create and Set all default information. Dont
            //Run any Alarm as Start button will take care of that.

            //Clear the click status
            clearAllWidgetsClickFlag();

            //Get Location info from Parent Intent
            getLocationFromParentCaller();

            //GetLocation info from Saved Info
            //If the Activity is called first time or before final location set
            //Shared pref is created and Default location of "Melbourne" is stored
            //
            Log.d("GAN_DBG","LOCATION SAVED INFO called");
            getLocationFromSavedInfo();

            //Set Default Start and End Time
            //This is and exception from else condition
            setDefaultStartEndTimerInfo();

            //Set Default Location Information
            // either from Saved info or Parent Intent
            setDefaultLocationInformation();
            //Set the Time and Date in the screen widgets
            setTimeDatelayout();
            //Enable the Time inputs
            enableTimeInputs(true, BLUE);
            //Enable the Location Inputs
            enableLocationInputs(true, BLUE);


        } else {

            //This is run if the alarm is already
            //running. All the info got from the shared pref
            //As the flags are set for the first time when
            //Start button is clicked



            //clear all the widget flags.Just testing it.
            clearAllWidgetsClickFlag();

            //Check again. This might have caused the issue.
            setDefaultLocationInformation();
            //Set all the Time Info in the screen
            setTimeDatelayout();
            //Toggle the Start button to stop
            timerstartButton.setText("Stop Alarm");
            //Disable all inputs
            enableTimeInputs(false,DKGRAY);
            //Calculate and Set the remaining time
            findtheremainingtime();
            //Start the timer
            starttheTimer();
        }
    }


    public void getLocationFromParentCaller(){

        //If Main activity or Menu creates then sends
        //the Location information
        Intent intent = getIntent();
        if (intent != null){
            location = intent.getStringExtra(PARENT_INTENT_LOCATION);
        }
        else{
            return;
        }

    }


    public void getLayoutWidgetsInClassVariables(){

        //Get the input layout fields to the class variables
        //Buttons
        timerstartButton = (Button) findViewById(R.id.button_timer_start);
        timerprofileButton = (Button) findViewById(R.id.button_timer_profile);
        //Dates
        startDateText= (TextView) findViewById(R.id.text_date_inputStart);
        endDateText= (TextView) findViewById(R.id.text_date_inputEnd);
        //Times
        startTimeText = (TextView) findViewById(R.id.text_timer_inputStart);
        endTimeText = (TextView) findViewById(R.id.text_timer_inputEnd);
        timerDisplay = (TextView) findViewById(R.id.text_timer_display);
        reminderMinutesText= (TextView) findViewById(R.id.text_timer_reminder_input);
        //Location
        locationInputText= (EditText) findViewById(R.id.text_location_place_input);
        //ProgressBar
        progressBar = (ProgressBar) findViewById(R.id.progressbar_circle);

    }

    public void enableLocationInputs(boolean boolvalue, int color){
        //Used to either enable or disable location input based on the args
        //Enable or Disable the Location Input
        locationInputText.setClickable(boolvalue);
        locationInputText.setEnabled(boolvalue);
        locationInputText.setTextColor(color);
    }


    public void enableTimeInputs(boolean boolvalue, int color){


        //Set all the Timer widgets inputs.
        startDateText.setClickable(boolvalue);
        startDateText.setEnabled(boolvalue);
        startDateText.setTextColor(color);

        endDateText.setClickable(boolvalue);
        endDateText.setEnabled(boolvalue);
        endDateText.setTextColor(color);

        startTimeText.setClickable(boolvalue);
        startTimeText.setEnabled(boolvalue);
        startTimeText.setTextColor(color);

        endTimeText.setClickable(boolvalue);
        endTimeText.setEnabled(boolvalue);
        endTimeText.setTextColor(color);

        reminderMinutesText.setClickable(boolvalue);
        reminderMinutesText.setEnabled(boolvalue);
        reminderMinutesText.setTextColor(color);

    }

    public void setDefaultLocationInformation(){

        locationInputText.setText(location);
    }


    public void setDefaultStartEndTimerInfo() {
        //Set Default Start and End Time
        //add the Keyboard disable program

        //Get the Current time for Reminder time calculation
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        inputStartTimeMSDate = calendar.getTime();



        //calendar.add(Calendar.DATE, 1);
        //calendar.add(Calendar.HOUR_OF_DAY, 1);
        calendar.add(Calendar.MINUTE,1);
        inputEndTimeMSDate = calendar.getTime();

        reminderHourOfDay = 0; //Hours
        reminderMinute = 0; //Minutes

    }


    public void setTimeDatelayout(){

        SimpleDateFormat dateformatter = new SimpleDateFormat("dd MMM yyyy");
        String startDateString = dateformatter.format(inputStartTimeMSDate);
        String endDateString = dateformatter.format(inputEndTimeMSDate);

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String startTimeString = formatter.format(inputStartTimeMSDate);
        String endTimeString = formatter.format(inputEndTimeMSDate);

        String reminderTimeStr = String.format(Locale.getDefault(),
                "%02d:%02d", reminderHourOfDay, reminderMinute);

        //Calculate ReminderTimeDate and set it
        //Check this again if it might cause issue
        //Reminder time is calculated again if the information is set
        //when the application reenters
        setReminderAlertTime(reminderHourOfDay, reminderMinute);


        //Set All the widgets
        //Set ReminderMinutes
        reminderMinutesText.setText(reminderTimeStr);
        //Set the Layout Timer text labels
        startDateText.setText(startDateString);
        endDateText.setText(endDateString);
        startTimeText.setText(startTimeString);
        endTimeText.setText(endTimeString);

    }

    public void setReminderAlertTime(int hourOfDay,int minutes){

        //Sets Reminder Time subtracting from the End date
        //and taking the hours from the reminder input widget set time.
        Calendar cal = Calendar.getInstance();
        cal.setTime(inputEndTimeMSDate);
        cal.add(Calendar.HOUR_OF_DAY, hourOfDay * -1); //Negative for decrease
        cal.add(Calendar.MINUTE,minutes * -1); //Negative for decrease
        //Check if the seconds need to be reduced
        cal.set(Calendar.SECOND, 0);

        //set reminder time in Date format and
        //Set with millisecs and not subtracted earlier
        //Conversion to Date and long format
        reminderTimeDate = new Date(cal.getTimeInMillis());
        reminderTime = cal.getTimeInMillis();

    }


    //Set Start DATE input text onClick Listener
    public void setStartDateOnClickListener() {
        startDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDateClicked= true;
                endDateClicked = false;

                DialogFragment datePicker = new TimerDateDialogueFragment();
                datePicker.show(getSupportFragmentManager(), DATEPICKERTAG);
            }
        });
    }
    //Set Start DATE input text onClick Listener
    public void setEndDateOnClickListener() {
        endDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDateClicked= false;
                endDateClicked = true;

                DialogFragment datePicker = new TimerDateDialogueFragment();
                datePicker.show(getSupportFragmentManager(), DATEPICKERTAG);
            }
        });
    }
    //Set Start Time input text onClick Listener
    public void setStartTimeOnClickListener() {
        startTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimeClicked = true;
                endTimeClicked = false;
                reminderTimeClicked = false;

                DialogFragment timePicker = new TimerDialogueFragment();
                timePicker.show(getSupportFragmentManager(), TIMEPICKERTAG);
            }
        });
    }

    //Set EndTime input text onClick Listener
    public void setEndTimeOnClickListener() {
        endTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimeClicked = false;
                endTimeClicked = true;
                reminderTimeClicked = false;

                DialogFragment timePicker = new TimerDialogueFragment();
                timePicker.show(getSupportFragmentManager(), TIMEPICKERTAG);
            }
        });
    }


    public void setReminderTimeOnClickListener(){
        reminderMinutesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimeClicked = false;
                endTimeClicked = false;
                reminderTimeClicked = true;

                DialogFragment timePicker = new TimerDialogueFragment();
                timePicker.show(getSupportFragmentManager(), TIMEPICKERTAG);
            }
        });
    }

    //Click the button in the Timer Activity to start the Timer
    //No return expected as of now
    public void setTimerStartButtonListerner() {
        timerstartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isTimerRunning) {
                    //If Alarm running, Disable inputs
                    pausetheTimer();
                    enableTimeInputs(true,BLUE);
                    isTimerRunning = false;

                } else {
                    //Set and Start the timer
                    timerstartButton.setText("Stop Alarm");
                    //Disable all inputs
                    enableTimeInputs(false,DKGRAY);
                    isTimerRunning = true;
                    //Set the reminder time
                    setReminderAlertTime(reminderHourOfDay,reminderMinute);

                    //Calculate the latest reminder time
                    //Probably adding this could solve the issue
                    //setReminderAlertTime(reminderHourOfDay,reminderMinute);

                    //Calculate and Set the remaining time
                    findtheremainingtime();

                    //Validate the times
                    validatetheTimes();
                    //Start the timer
                    starttheTimer();
                }
            }
        });

    }

    public void setTimerProfileButtonListerner(){
        timerprofileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Get the latest input location from the input text
                location = locationInputText.getText().toString();

                //Update the info to the Firebase
                startupdateProfileHistory();
            }
        });
    }


    public void starttheTimer() {
        //While running disable the edit status unless the
        //reset button is clicked or restarted
        //Start or Pause the timer


        if(timesareFine){
            //Set the timer flag "On"
            isTimerRunning = true;

            //Set the Notification Alarm Timer Alert Timer
            setNotificationAlert();

            //Make the Progress Circle Emtpy start
            progressBar.setProgress(0);

            //Set total time outside on Tick here
            //To get the Total for % calculation
            final long totaltime = remainingTime;


            //Set and Start the Count Down Timer
            countDownTimer = new CountDownTimer(remainingTime, ONESECONDINTERVAL) {
                @Override
                public void onTick(long millisUntilFinished) {
                    remainingTime = millisUntilFinished;
                    updateProgressBar(totaltime,remainingTime);
                    updateTickerCountDown();

                }
                @Override
                public void onFinish() {
                    isTimerRunning = false;
                    pausetheTimer();
                    enableTimeInputs(true,BLUE);
                    progressBar.setProgress(100);
                }
            };
            //Important. Start the timer
            countDownTimer.start();
        }


    }
    public void updateProgressBar(long totalTime,long remainingTime){

        //Calculate the percentage progress
        long percentageFinished = 100 - ((remainingTime * 100) / totalTime);

        //set Progress only available from certain version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress((int) percentageFinished);
        }
    }

    public void findtheremainingtime(){
        // Set Remaining Time. This is used for Clock Ticker
        Calendar cal = Calendar.getInstance();
        remainingTime = inputEndTimeMSDate.getTime() - cal.getTimeInMillis();
    }

    public void validatetheTimes(){

        //Use Remaining time and validate
        int hour_millisec = (int) remainingTime / 1000;
        int hours = (int) (hour_millisec) / 3600;

        //Here Minutes and Seconds are not used but
        //Included for reference and future use
        int minutes_hour = (int) hour_millisec % 3600;
        int minutes = (int) (minutes_hour ) / 60;
        int second_minutes = (int) minutes_hour % 60;
        int seconds = second_minutes;

        //Used to take the current time
        Calendar caltemp = Calendar.getInstance();


        currentTimeMSDate = caltemp.getTime();

        //If input End time greater than Current time
        if (inputEndTimeMSDate.before(currentTimeMSDate) ||
                inputEndTimeMSDate.equals(currentTimeMSDate)){
            Toast.makeText(this,"End time Should be greater than current time",
                    Toast.LENGTH_LONG).show();

            pausetheTimer();
            enableTimeInputs(true,BLUE);
            timesareFine = false;
            return;
        }

        //If input End time is greater than 4 days
        //As of now restricting the max time to 4 days
        if(hours > 99) {
            Toast.makeText(this,
                    "End Time should be within 4 days from current time",
                    Toast.LENGTH_LONG).show();
            pausetheTimer();
            enableTimeInputs(true,BLUE);
            timesareFine = false;
            return;
        }

        //If Start time is greater than End time
        if(inputStartTimeMSDate.after(inputEndTimeMSDate)) {
            Toast.makeText(this,
                    "Start Time should be less than the End time",
                    Toast.LENGTH_LONG).show();
            pausetheTimer();
            enableTimeInputs(true,BLUE);
            timesareFine = false;
            return;
        }

        //Reminder time is not inbetween current and end time
        if(reminderTimeDate.before(currentTimeMSDate) ||
                reminderTimeDate.after(inputEndTimeMSDate)) {

            Toast.makeText(this,
                    "Reminder time Should be between current time and End Time",
                    Toast.LENGTH_LONG).show();
            pausetheTimer();
            enableTimeInputs(true,BLUE);
            timesareFine = false;
            return;
        }

        timesareFine = true;

    }

    public void pausetheTimer() {
        //reset the Timer

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (alarmManager != null){
            alarmManager.cancel(pendingIntent);
        }
        if (alarmManagerReminder != null){
            alarmManagerReminder.cancel(pendingIntentReminder);
        }

        remainingTime = 0;
        updateTickerCountDown();
        timerstartButton.setText("START ALARM");
        isTimerRunning = false;
        progressBar.setProgress(100);
        enableTimeInputs(true,BLUE);

        //Not sure if this is required
        //savetheinfoinSharedPref();

    }

    public void updateTickerCountDown() {

        //Update the ticker
        int hour_millisec = (int) remainingTime / 1000;
        int hours = (int) (hour_millisec) / 3600;

        int minutes_hour = (int) hour_millisec % 3600;
        int minutes = (int) (minutes_hour ) / 60;

        int second_minutes = (int) minutes_hour % 60;
        int seconds = second_minutes;

        String remainingTimeTotal = String.format(Locale.getDefault(),
                "%02d:%02d:%02d", hours, minutes, seconds);

        timerDisplay.setText(remainingTimeTotal);
    }


    public void setNotificationAlert(){

        //Alarm Manager and Main Alarm function

        Calendar calendar = Calendar.getInstance();

        if(inputEndTimeMSDate.equals(calendar.getTime())){

            //No action;

        } else{
            //Set the Alarm Alarm for End Time
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, TimerNoticeReceiver.class);
            pendingIntent =
                    PendingIntent.getBroadcast(this, 1, intent, 0);
            //First Alarm - For End time
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    inputEndTimeMSDate.getTime(), pendingIntent);

            Toast.makeText(this,"End Time Alarm is Set",Toast.LENGTH_SHORT).show();

        }

        if(reminderTimeDate.equals(calendar.getTime()) ||
                ((reminderHourOfDay ==0) && (reminderMinute == 0)) ||
                reminderTimeDate.before(calendar.getTime())){
        } else {
            //Second Alarm - For the Reminder time
            alarmManagerReminder = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intentReminder = new Intent(this, TimerNoticeReceiver.class);
            pendingIntentReminder =
                    PendingIntent.getBroadcast(this, 2, intentReminder,
                            0);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    reminderTimeDate.getTime(), pendingIntentReminder);

            Toast.makeText(this,"Reminder Alarm is Set",Toast.LENGTH_SHORT).show();
        }


    }

    public void startupdateProfileHistory(){

        //Create instance of Profile Manager
        TProfileMgr profileMgr = new TProfileMgr();

        if(profileMgr.getUserID() ==null){
            Toast.makeText(this,"User Account login required.",
                    Toast.LENGTH_SHORT).show();

        } else {
            //Time Stamps
            inputStartTimestamp = new Timestamp(inputStartTimeMSDate);
            inputEndTimestamp = new Timestamp(inputEndTimeMSDate);

            parkevent = new Profile(profileMgr.getUserID(), location,
                    inputStartTimestamp, inputEndTimestamp);

            profileMgr.addParkEvent(parkevent);
            int returnvalue = profileMgr.updateProfileHistory();

            if (returnvalue ==1){
                Toast.makeText(this, "Park Info Saved", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(this, "Error: Park Info Not Saved",
                        Toast.LENGTH_SHORT).show();
            }


        }
    }

    //Include logic for Closure of the application. Also for On pause and On Stop
    //Override another function to find if time is not set and the dialogue is cancelled
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        String timeStr;
        SimpleDateFormat formatter;
        Calendar cal = Calendar.getInstance();

        //Check and Include start and End time time difference positive only
        //Set the time on the Corresponding Text
        if (startTimeClicked) {
            //Set the time for End date + New End Time. Order is important.
            cal.setTime(inputStartTimeMSDate);        //->Should be first
            cal.set(Calendar.HOUR_OF_DAY,hourOfDay);       //->Should be second
            cal.set(Calendar.MINUTE, minute);       //->Should be third
            cal.set(Calendar.SECOND, 0);


            inputStartTimeMSDate = new Date(cal.getTimeInMillis());

            timeStr = formatDatetoStr(inputStartTimeMSDate, "HH:mm");

            //Set the Display
            startTimeText.setText(timeStr);
        }
        if (endTimeClicked) {

            cal.setTime(inputEndTimeMSDate);        //->Should be first
            cal.set(Calendar.HOUR_OF_DAY,hourOfDay);       //->Should be second
            cal.set(Calendar.MINUTE, minute);       //->Should be third
            cal.set(Calendar.SECOND, 0);


            inputEndTimeMSDate = new Date(cal.getTimeInMillis());


            timeStr = formatDatetoStr(inputEndTimeMSDate, "HH:mm");

            //Update the reminder time
            setReminderAlertTime(reminderHourOfDay,reminderMinute);


            //Set the Display
            endTimeText.setText(timeStr);
        }
        if (reminderTimeClicked) {


            //Calcluate Reminder Alarm Setting Time
            //Subtracts hourofday and minutes from the inputEndTime
            //Calculate and Set reminderTimeDate (date format)
            //Calculate and Set reminderTime (long format)
            reminderHourOfDay = hourOfDay;
            reminderMinute = minute;
            //Update the reminder time
            setReminderAlertTime(reminderHourOfDay,reminderMinute);


            //If reminder time is close to 12AM then there is a chance
            //of getting the previous day so don't use it for Time set
            //Use directly the Hour of Day and minute directly
            //Format the string

            timeStr = String.format("%02d:%02d", hourOfDay, minute);

            reminderMinutesText.setText(timeStr);
        }

        //Reset all the Time flags
        startTimeClicked = false;
        endTimeClicked = false;
        reminderTimeClicked = false;
    }

    @Override
    public void onCancel(DialogInterface dialog) {

        startTimeClicked = false;
        endTimeClicked = false;
        reminderTimeClicked = false;

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {


        //Check and Include start and End time time difference positive only
        //Set the time on the Corresponding Text

//        Date tempDate;
        String dateStr;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");


        if (startDateClicked) {
            //Set the time
            cal.setTime(inputStartTimeMSDate);


            cal.set(Calendar.YEAR,year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DATE, dayOfMonth);
            cal.set(Calendar.SECOND, 0);

            inputStartTimeMSDate = new Date(cal.getTimeInMillis());

            dateStr = formatter.format(inputStartTimeMSDate);

            startDateText.setText(dateStr);
        }
        if (endDateClicked) {
            cal.setTime(inputEndTimeMSDate);

            cal.set(Calendar.YEAR,year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DATE, dayOfMonth);
            cal.set(Calendar.SECOND, 0);

            inputEndTimeMSDate = new Date(cal.getTimeInMillis());
            dateStr = formatter.format(inputEndTimeMSDate);

            //Update the reminder time
            setReminderAlertTime(reminderHourOfDay,reminderMinute);

            endDateText.setText(dateStr);
        }

        startDateClicked = false;
        endDateClicked = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user_center, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        FirebaseAuth firebaseauth;

        firebaseauth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseauth.getCurrentUser();

        if(user==null){
            menu.findItem(R.id.logout).setVisible(false);
            menu.findItem(R.id.login).setVisible(true);
        }else {
            menu.findItem(R.id.logout).setVisible(true);
            menu.findItem(R.id.login).setVisible(false);
        }

        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException e) {
                } catch (Exception e) {
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.user_profile:
                Intent intent_profile = new Intent(this ,ProfileActivity.class);
                item.setIntent(intent_profile);
                startActivity(intent_profile);
                return true;
            case R.id.find_my_car:
                Intent intent_find = new Intent(this ,FindMyCarActivity.class);
                item.setIntent(intent_find);
                startActivity(intent_find);
                return true;
            case R.id.information:
                Intent intent_info = new Intent(this ,InformationActivity.class);
                item.setIntent(intent_info);
                startActivity(intent_info);
                return true;
            case R.id.timer:
                return true;
            case R.id.login:
                Intent intent_login = new Intent(this , LoginActivity.class);
                item.setIntent(intent_login);
                startActivity(intent_login);
                return true;
            case R.id.logout:
                Intent intent_logout = new Intent(this , LogoutActivity.class);
                item.setIntent(intent_logout);
                startActivity(intent_logout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        savetheinfoinSharedPref();
    }

    @Override
    protected void onStop() {
        //Call the Super
        super.onStop();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        //stoptheCounterTemp();
        savetheinfoinSharedPref();
    }

    @Override
    public void finish(){
        super.finish();

    }


    private void savetheinfoinSharedPref() {

        try {

            //Get the Shared Preference Info
            SharedPreferences sharedPref = getSharedPreferences(SHAREDPREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor spEdit = sharedPref.edit();

            //Save the required info
            spEdit.putBoolean(SHAREDPREF_ISTIMERRUNNING, isTimerRunning);
            spEdit.putLong(SHAREDPREF_STARTTIMELONG, inputStartTimeMSDate.getTime());
            spEdit.putLong(SHAREDPREF_ENDTIMELONG, inputEndTimeMSDate.getTime());
            spEdit.putLong(SHAREDPREF_REMINDERTIMELONG, reminderTimeDate.getTime());
            spEdit.putInt(SHAREDPREF_REMINDERHOURINT, reminderHourOfDay);
            spEdit.putInt(SHAREDPREF_REMINDERMINUTEINT, reminderMinute);
            spEdit.putString(SHAREDPREF_LOCATION,location);

            //Complete Save the information
            spEdit.apply();


        } catch (Exception e){
        }
    }


    public void getSavedSharedPreferences(){
        //Get all the Shared Preference Values
        try{
            SharedPreferences sharedPref = getSharedPreferences(SHAREDPREF_NAME, MODE_PRIVATE);

            isTimerRunning = sharedPref.getBoolean(SHAREDPREF_ISTIMERRUNNING, false);
            long startTime = sharedPref.getLong(SHAREDPREF_STARTTIMELONG, 0);
            long endTime = sharedPref.getLong(SHAREDPREF_ENDTIMELONG, 0);
            reminderTime = sharedPref.getLong(SHAREDPREF_REMINDERTIMELONG, 0);
            reminderHourOfDay = sharedPref.getInt(SHAREDPREF_REMINDERHOURINT, 0);
            reminderMinute = sharedPref.getInt(SHAREDPREF_REMINDERMINUTEINT,0);
            location = sharedPref.getString(SHAREDPREF_LOCATION,DEFAULT_LOCATION);

            //Convert the received long to Date - Important variables
            inputStartTimeMSDate = new Date(startTime);
            inputEndTimeMSDate = new Date(endTime);
            reminderTimeDate = new Date(reminderTime);


        } catch (Exception e){
        }
    }


    public void getLocationFromSavedInfo(){

        Log.d("GAN_DBG","getLocationfromSaved Info Entered");

        try{
            //Shared pref name is "location"
            SharedPreferences sharedPrefLocation =
                    getSharedPreferences(SHAREDPREF_LOCATION,MODE_PRIVATE);
            String tempLocation = sharedPrefLocation
                    .getString(SHAREDPREF_LOCATION,DEFAULT_LOCATION);

            Log.d("GAN_DBG","Temp Location: " +tempLocation);


            if(tempLocation.equals(DEFAULT_LOCATION) ||
                    tempLocation.equals(null) ||
                    tempLocation.equals("")){
                saveLocationSharedInfo(DEFAULT_LOCATION);
            }else{
                location = tempLocation;
                Log.d("GAN_DBG","Location is set");
            }

        }catch (Exception e){
        Log.d("GAN_DBG","SHARED GET INFO EXCEPTION ERROR");
        }

    }

    public void saveLocationSharedInfo(String locationtemp){

        Log.d("GAN_DBG", "Saved Shared Pref Location etered");
        //Shared pref name is "locationstring"
        SharedPreferences sharedPrefLocation =
                getSharedPreferences(SHAREDPREF_LOCATION,MODE_PRIVATE);
        SharedPreferences.Editor spEdit = sharedPrefLocation.edit();

        //Shared pref variable name is also "locationstring"
        //Save a default Location
        spEdit.putString(SHAREDPREF_LOCATION,locationtemp);

        //Complete Save the information
        spEdit.apply();
    }


    public String formatDatetoStr(Date dt, String strFormat){
        SimpleDateFormat formatter = new SimpleDateFormat(strFormat);
        String str = formatter.format(dt);
        return str;
    }

    public String formatMillisecstoStr(long millisecs, String strFormat){
        //For Future Use
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        String str = formatter.format(new Date(millisecs));
        return str;
    }

    public Calendar formatMillisecstoCal(long millisecs){
        //For Future Use
        //Use with Caution. This will have the today's date
        //as the date component but include the time with the input
        //Verify it properly.

        int hour_millisec = (int) millisecs / 1000;
        int hours = (int) (hour_millisec) / 3600;

        int minutes_hour = (int) hour_millisec % 3600;
        int minutes = (int) (minutes_hour ) / 60;

        int second_minutes = (int) minutes_hour % 60;
        int seconds = second_minutes;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,hours);
        calendar.set(Calendar.MINUTE,minutes);
        //calendar.set(Calendar.SECOND,seconds);
        calendar.set(Calendar.SECOND, 0);

        return (calendar);
    }

    public void clearAllWidgetsClickFlag(){
        //Widget click flags are reset
        //Don't set the Input times and Location as it might be
        //Received from the Shared Pref. If it is not they will
        //be set to default. So only clear the flags
        startTimeClicked = false;
        endTimeClicked = false;
        startDateClicked = false;
        endDateClicked = false;
        reminderTimeClicked = false;
    }
}

