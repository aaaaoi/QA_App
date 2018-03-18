package jp.techacademy.saori.tawada.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class FavoriteActivity extends AppCompatActivity {

    private ListView mListView;
    private QuestionsListAdapter mAdapter;
    private int mGenre = 0;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFavoriteRef;
    private DatabaseReference mGenreRef;
    private ArrayList<String> mFavoriteArrayList;
    private ArrayList<Question> mQuestionArrayList;
    private HashMap<String, String> mHashMap;

    private ChildEventListener mFavoriteEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String genre = (String) map.get("genre");
            mHashMap.put(dataSnapshot.getKey(), genre);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mChildEventListener;

    {
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                HashMap map = (HashMap) dataSnapshot.getValue();
                String title = (String) map.get("title");
                String body = (String) map.get("body");
                String name = (String) map.get("name");
                String uid = (String) map.get("uid");
                String imageString = (String) map.get("image");
                byte[] bytes;
                if (imageString != null) {
                    bytes = Base64.decode(imageString, Base64.DEFAULT);
                } else {
                    bytes = new byte[0];
                }

                ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                HashMap answerMap = (HashMap) map.get("answers");
                if (answerMap != null) {
                    for (Object key : answerMap.keySet()) {
                        HashMap temp = (HashMap) answerMap.get((String) key);
                        String answerBody = (String) temp.get("body");
                        String answerName = (String) temp.get("name");
                        String answerUid = (String) temp.get("uid");
                        Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                        answerArrayList.add(answer);
                    }
                }

                if (mHashMap.containsKey(dataSnapshot.getKey())) {
                    Question favQuestion = new Question(title, body, name, uid, dataSnapshot.getKey(), Integer.valueOf(mHashMap.get(dataSnapshot.getKey())), bytes, answerArrayList);
                    mQuestionArrayList.add(favQuestion);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        //ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mHashMap = new HashMap<>();
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFavoriteRef = mDatabaseReference.child(Const.FavoritePATH).child(user.getUid());
        mFavoriteRef.addChildEventListener(mFavoriteEventListener);

        for (int i = 1; i <= 4; i++) {
            DatabaseReference mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(i));
            mGenreRef.addChildEventListener(mFavoriteEventListener);
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
    }
}
