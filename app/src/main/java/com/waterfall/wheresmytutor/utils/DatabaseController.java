package com.waterfall.wheresmytutor.utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.waterfall.wheresmytutor.R;
import com.waterfall.wheresmytutor.models.Meeting;
import com.waterfall.wheresmytutor.models.Student;
import com.waterfall.wheresmytutor.models.Tutor;
import com.waterfall.wheresmytutor.models.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseController {
    final FirebaseDatabase db;
    final Context context;
    private final String USERS_PATH = "Users";
    private final String STATUS_PATH = "status";
    private final String MEETINGS_PATH = "meetings";
    private final String USER_TYPE_PATH = "userType";
    private final String USER_MY_TUTORS_IDS = "myTutorsIds";
    private final String USER_MY_MEETING_IDS = "meetingIds";
    private final Map<String, String> DAYS_TO_DAYS_PATH;
    private final Map<String, Integer> DAYS_TO_CALENDER_ENUM;

    public DatabaseController(Context context) {
        this.db = FirebaseDatabase.getInstance("https://wheres-my-tutor-n0906911-default-rtdb.europe-west1.firebasedatabase.app/");
        this.context = context;
        DAYS_TO_DAYS_PATH = new HashMap<String, String>() {{
            put(context.getString(R.string.day_monday), "monTimeSlots");
            put(context.getString(R.string.day_tuesday), "tueTimeSlots");
            put(context.getString(R.string.day_wednesday), "wedTimeSlots");
            put(context.getString(R.string.day_thursday), "thurTimeSlots");
            put(context.getString(R.string.day_friday), "friTimeSlots");
        }};

        DAYS_TO_CALENDER_ENUM = new HashMap<String, Integer>() {{
            put(context.getString(R.string.day_monday), Calendar.MONDAY);
            put(context.getString(R.string.day_tuesday), Calendar.TUESDAY);
            put(context.getString(R.string.day_wednesday), Calendar.WEDNESDAY);
            put(context.getString(R.string.day_thursday), Calendar.THURSDAY);
            put(context.getString(R.string.day_friday), Calendar.FRIDAY);
        }};
    }

    public void getMyTutorsIds(String userId, ListCallback callback)
    {
        db.getReference(USERS_PATH).child(userId).child(USER_MY_TUTORS_IDS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {};
                List<String> ids = snapshot.getValue(genericTypeIndicator);
                callback.onCallback(ids != null ? ids : new ArrayList()); // return empty list if null
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createErrorAlertDialog(error);
            }
        });
    }
    public void postMyTutorsIds(String userId, List<String> tutorIds)
    {
        db.getReference(USERS_PATH).child(userId).child(USER_MY_TUTORS_IDS).setValue(tutorIds);
    }

    public void getMyTutors(String userId, ListCallback callback)
    {
        DatabaseReference allUsersRef = db.getReference(USERS_PATH);
        GenericTypeIndicator<List<String>> genericStringTypeIndicator = new GenericTypeIndicator<List<String>>() {};

        allUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> ids = snapshot.child(userId).child(USER_MY_TUTORS_IDS).getValue(genericStringTypeIndicator);

                ArrayList<Tutor> tutors = new ArrayList<>();
                for (DataSnapshot item : snapshot.getChildren())
                {
                    if(ids == null) // return empty tutor list as no ids
                        break;

                    if(ids.contains(item.getKey()))
                        tutors.add(item.getValue(Tutor.class));
                }
                callback.onCallback(tutors);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createErrorAlertDialog(error);
            }
        });
    }

    public void getAllTutors(ListCallback callback)
    {
        db.getReference(USERS_PATH).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Tutor> tutors = new ArrayList<>();
                for (DataSnapshot item : snapshot.getChildren())
                {
                    String userType = item.child(USER_TYPE_PATH).getValue(String.class);
                    if(userType.equals(context.getString(R.string.user_type_tutor)))
                        tutors.add(item.getValue(Tutor.class));
                }
                callback.onCallback(tutors);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createErrorAlertDialog(error);
            }
        });
    }

    public void getMeeting(String meetingId, MeetingCallback meetingCallback)
    {
        db.getReference(MEETINGS_PATH).child(meetingId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                meetingCallback.onCallback(snapshot.getValue(Meeting.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createErrorAlertDialog(error);
            }
        });
    }

    public void getMyMeetings(String userId, ListCallback callback) {
        GenericTypeIndicator<List<String>> genericStringTypeIndicator = new GenericTypeIndicator<List<String>>() {};
        db.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> meetingIds = snapshot.child(USERS_PATH).child(userId).child(USER_MY_MEETING_IDS).getValue(genericStringTypeIndicator);

                ArrayList<Meeting> meetings = new ArrayList<>();
                if (meetingIds != null) {
                    for (String id: meetingIds) {
                        meetings.add(snapshot.child(MEETINGS_PATH).child(id).getValue(Meeting.class));
                    }
                }
                callback.onCallback(meetings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createErrorAlertDialog(error);
            }
        });
    }

    public void getDaysOfAvailableTimeSlots(String tutorId, ListCallback listCallback)
    {
        db.getReference(USERS_PATH).child(tutorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Integer> availableDays = new ArrayList<>();
                for (String dayOfTheWeek: context.getResources().getStringArray(R.array.days_of_week)) {
                    if (snapshot.child(DAYS_TO_DAYS_PATH.get(dayOfTheWeek)).exists())
                        availableDays.add(DAYS_TO_CALENDER_ENUM.get(dayOfTheWeek));
                }
                listCallback.onCallback(availableDays);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createErrorAlertDialog(error);
            }
        });
    }

    public void postTimeSlotsByDay(String tutorId, String day, List<String> timeSlots)
    {
        db.getReference(USERS_PATH).child(tutorId).child(DAYS_TO_DAYS_PATH.get(day)).setValue(timeSlots);
    }

    public void getTutorTimeSlotsByDay(String tutorId, String day, ListCallback listCallback)
    {
        db.getReference(USERS_PATH).child(tutorId).child(DAYS_TO_DAYS_PATH.get(day)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {};
                List<String> timeSlots = snapshot.getValue(genericTypeIndicator);
                listCallback.onCallback(timeSlots != null ? timeSlots : new ArrayList<String>());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createErrorAlertDialog(error);
            }
        });
    }

    public void getTutor(String tutorId, TutorCallback tutorCallback)
    {
        db.getReference(USERS_PATH).child(tutorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tutorCallback.onCallback(snapshot.getValue(Tutor.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createErrorAlertDialog(error);
            }
        });
    }

    public void postTutor(String tutorId, Tutor tutor)
    {
        db.getReference(USERS_PATH).child(tutorId).setValue(tutor);
    }

    public void getStudent(String studentId, StudentCallback studentCallback)
    {
        db.getReference(USERS_PATH).child(studentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentCallback.onCallback(snapshot.getValue(Student.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createErrorAlertDialog(error);
            }
        });
    }

    public void postStudent(String studentId, Student student)
    {
        db.getReference(USERS_PATH).child(studentId).setValue(student);
    }

    public void getUserType(String userId, StringCallback userTypeCallback)
    {
        db.getReference(USERS_PATH).child(userId).child(USER_TYPE_PATH).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userTypeCallback.onCallback(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                createErrorAlertDialog(error);
            }
        });
    }

    public void postTutorStatus(String tutorId, String status)
    {
        Map<String, Object> newStatusMap  = new HashMap<>();
        newStatusMap.put(STATUS_PATH, status);
        db.getReference(USERS_PATH).child(tutorId).updateChildren(newStatusMap);
    }

    public void postTutorLocation(String userId, double latitude, double longitude)
    {
        Map<String, Object> newStatusMap  = new HashMap<String, Object>()
        {{
            put("latitude", latitude);
            put("longitude", longitude);
        }};
        db.getReference(USERS_PATH).child(userId).updateChildren(newStatusMap);
    }

    public void postUser(String userId, User newUser, BooleanCallback isSuccessful)
    {
        db.getReference(USERS_PATH).child(userId).setValue(newUser).addOnCompleteListener(task -> isSuccessful.onCallback(task.isSuccessful()));
    }

    public void postMeetingRequest(String newMeetingId, Meeting meeting) {
        db.getReference(MEETINGS_PATH).child(newMeetingId).setValue(meeting);
    }

    public void postMeetingIds(List<String> meetingIds, String userId) {
        db.getReference(USERS_PATH).child(userId).child(USER_MY_MEETING_IDS).setValue(meetingIds);
    }

    public void updateMeeting(Meeting meeting) {
        db.getReference(MEETINGS_PATH).child(meeting.getMeetingId()).setValue(meeting);
    }

    public void deleteMeetingRequest(Meeting meetToDelete, String currentUserId, String otherUserId, BooleanCallback successfulCallback)
    {
        GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {};
        db.getReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                successfulCallback.onCallback(true);
                // Remove id from current users list
                List<String> meetingIds = snapshot.child(USERS_PATH).child(currentUserId).child(USER_MY_MEETING_IDS).getValue(genericTypeIndicator);
                meetingIds.remove(meetToDelete.getMeetingId());
                postMeetingIds(meetingIds, currentUserId);

                // check to see if other user has removed their ID
                for (DataSnapshot item : snapshot.child(USERS_PATH).child(otherUserId).child(USER_MY_MEETING_IDS).getChildren()) {
                    String meetingId = item.getValue(String.class);

                    if(meetingId.equals(meetToDelete.getMeetingId()))
                        return; // return because the other user is still using the meeting request.
                }
                // if the meeting does not exist for the other user, delete the meeting from the system.
                snapshot.child(MEETINGS_PATH).child(meetToDelete.getMeetingId()).getRef().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                successfulCallback.onCallback(false);
                createErrorAlertDialog(error);
            }
        });
    }

    public void postMeetingLocation(String meetingId, double latitude, double longitude)
    {
        Map<String, Object> newStatusMap  = new HashMap<String, Object>()
        {{
            put("latitude", latitude);
            put("longitude", longitude);
        }};
        db.getReference(MEETINGS_PATH).child(meetingId).updateChildren(newStatusMap);
    }

    public interface BooleanCallback {
        void onCallback(boolean bool);
    }

    public interface StringCallback {
        void onCallback(String string);
    }

    public interface TutorCallback {
        void onCallback(Tutor tutor);
    }

    public interface StudentCallback {
        void onCallback(Student student);
    }

    public interface MeetingCallback {
        void onCallback(Meeting meeting);
    }

    public interface ListCallback {
        void onCallback(List list);
    }

    private void createErrorAlertDialog(DatabaseError error){
        new MaterialAlertDialogBuilder(context)
                .setTitle("Error " + error.getCode() )
                .setMessage(error.getMessage());
    }
}
