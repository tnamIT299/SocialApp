package com.trinhthanhnam.mysocialapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinhthanhnam.mysocialapp.adapter.AdapterUser;
import com.trinhthanhnam.mysocialapp.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment {

    EditText edtSearch;
    ImageButton imgBtnSearch;
    FloatingActionButton group_add_fab;

    RecyclerView recyclerView;
    AdapterUser adapterUser;
    List<User> userList;

    FirebaseAuth firebaseAuth;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UsersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UsersFragment newInstance(String param1, String param2) {
        UsersFragment fragment = new UsersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_users, container, false);

        //ánh xạ
         recyclerView = view.findViewById(R.id.recyclerViewUser);
         recyclerView.setHasFixedSize(true);
         recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
         imgBtnSearch = view.findViewById(R.id.imgBtn_Search);
         edtSearch = view.findViewById(R.id.edtSearch);
         group_add_fab = view.findViewById(R.id.group_add_fab);

         group_add_fab.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                    //start activity
                    startActivity(new Intent(getActivity(), GroupCreateActivity.class));
             }
         });


         //init
        userList = new ArrayList<>();
        addEvent();
        //get all users
        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        //get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    //get all users except currently signed in user
                    if (user != null && fUser != null && user.getUid() != null && !user.getUid().equals(fUser.getUid())){
                        userList.add(user);
                    }
                    //adapter
                    adapterUser = new AdapterUser(getActivity(), userList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addEvent(){
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString())) {
                    searchUsers(s.toString());
                } else {
                    getAllUsers();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void searchUsers(String query) {
        // get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        // get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    // get all users except currently signed in user
                    FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (fUser != null && user != null && user.getUid() != null && !user.getUid().equals(fUser.getUid())){
                        if (user.getName().toLowerCase().contains(query.toLowerCase()) ||
                                user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                            userList.add(user);
                        }
                    }
                }
                // adapter
                adapterUser = new AdapterUser(getActivity(), userList);
                // set adapter to recyclerview
                recyclerView.setAdapter(adapterUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

