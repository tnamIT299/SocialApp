package com.trinhthanhnam.mysocialapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinhthanhnam.mysocialapp.adapter.AdapterGroupChat;
import com.trinhthanhnam.mysocialapp.model.GroupChat;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupChatFragment extends Fragment {
    RecyclerView groupChatRv;
    FirebaseAuth firebaseAuth;
    ArrayList<GroupChat> groupChatList;
    AdapterGroupChat adapterGroupChat;
    EditText searchGroupEt;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public GroupChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupChatFragment newInstance(String param1, String param2) {
        GroupChatFragment fragment = new GroupChatFragment();
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
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);

        groupChatRv = view.findViewById(R.id.groupRv);
        searchGroupEt = view.findViewById(R.id.edtSearch);
        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupChatList();
        searchGroup();
        return view;
    }

    private void searchGroup() {
        searchGroupEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString())) {
                    searchGroupChatList(s.toString());
                } else {
                    loadGroupChatList();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadGroupChatList() {
        groupChatList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                   if(ds.child("Participants").child(firebaseAuth.getUid()).exists()) {
                       GroupChat model = ds.getValue(GroupChat.class);
                       groupChatList.add(model);
                   }
                }
                adapterGroupChat = new AdapterGroupChat(getActivity(), groupChatList);
                groupChatRv.setAdapter(adapterGroupChat);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchGroupChatList(String query) {
        groupChatList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                groupChatList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if(ds.child("Participants").child(firebaseAuth.getUid()).exists()) {
                        if (ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())) {
                            GroupChat model = ds.getValue(GroupChat.class);
                            groupChatList.add(model);
                        }

                    }
                }
                adapterGroupChat = new AdapterGroupChat(getActivity(), groupChatList);
                groupChatRv.setAdapter(adapterGroupChat);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}