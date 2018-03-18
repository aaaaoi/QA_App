package jp.techacademy.saori.tawada.qa_app;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;

    private FloatingActionButton fabFavorite;
    private boolean mFavoriteFlag = false;

    private ChildEventListener mFavoriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            mFavoriteFlag = true;
            fabFavorite.setImageResource(android.R.drawable.btn_star_big_on);
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

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for (Answer answer : mQuestion.getAnswers()) {
                //同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        //渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        //ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    //ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    //Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });


        fabFavorite = (FloatingActionButton) findViewById(R.id.fabFavorite);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            fabFavorite.setVisibility(View.INVISIBLE);
        } else {
            fabFavorite.setVisibility(View.VISIBLE);
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference userRef = databaseReference.child(Const.FavoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());
            userRef.addChildEventListener(mFavoriteListener);
        }

        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference();
        DatabaseReference favoriteRef = databaseReference1.child(Const.FavoritePATH).child(user.getUid()).child(String.valueOf(mQuestion.getQuestionUid()));
        String favoriteUid = mQuestion.getQuestionUid().toString();
        DatabaseReference questionRef = databaseReference1.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid());
        String questionUid = mQuestion.getQuestionUid().toString();
        if (favoriteRef.equals(questionUid)) {
            fabFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            mFavoriteFlag = true;
        } else {
            fabFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            mFavoriteFlag = false;
        }

        fabFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFavoriteFlag == false) {
                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference favoriteRef = databaseReference1.child(Const.FavoritePATH).child(user.getUid()).child(String.valueOf(mQuestion.getQuestionUid()));
                    Map<String, String> date = new HashMap<String, String>();
                    String favoriteUid = mQuestion.getQuestionUid().toString();
                    date.put("genre", String.valueOf(mQuestion.getGenre()));
                    favoriteRef.setValue(date);

                    fabFavorite.setImageResource(android.R.drawable.btn_star_big_on);

                    Snackbar.make(v, "お気に入りに追加しました", Snackbar.LENGTH_LONG).show();
                } else {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference favoriteRef = databaseReference.child(Const.FavoritePATH).child(user.getUid()).child(String.valueOf(mQuestion.getQuestionUid()));
                    favoriteRef.setValue(null);

                    fabFavorite.setImageResource(android.R.drawable.btn_star_big_off);

                    Snackbar.make(v, "お気に入りを解除しました", Snackbar.LENGTH_LONG).show();
                }
                mFavoriteFlag = !mFavoriteFlag;
            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = databaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }
}
