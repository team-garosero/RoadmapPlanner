package com.garosero.android.hobbyroadmap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.garosero.android.hobbyroadmap.data.AsteriskPasswordTransformationMethod;
import com.garosero.android.hobbyroadmap.main.MainActivity;
import com.garosero.android.hobbyroadmap.network.NetworkFactory;
import com.garosero.android.hobbyroadmap.network.request.ReadUserRequest;
import com.garosero.android.hobbyroadmap.network.request.RequestListener;
import com.garosero.android.hobbyroadmap.network.response.UserResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    private String uid;
    private Button  btn_login, btn_signup;
    private TextView tv_login_failed;
    private EditText et_email, et_password;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference ref;

    // google login
    private ImageButton signInButton; // google login button
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGNIN = 1;

    private long lastTimeBackPressed; //뒤로가기 버튼이 클릭된 시간


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth =  FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();

        //버튼 등록하기
        btn_signup = findViewById(R.id.btn_signup);
        btn_login = findViewById(R.id.btn_login);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        tv_login_failed = findViewById(R.id.tv_login_failed);

        btn_login.setOnClickListener(this);
        btn_signup.setOnClickListener(this);
        et_password.setTransformationMethod(new AsteriskPasswordTransformationMethod());

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("384536585917-u0i5k9j9hafip4k0rpbnc1sf6tpi43k8.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton = findViewById(R.id.btn_login_google);
        signInButton.setOnClickListener(this);

        // Kakao Login
//        KakaoSdk.init(this, "8f02641c235286b1946a98f165197271");
//        KakaoLoginHelper.Companion.login(this);

        // 로그인 되어 있으면 main Activity로 이동
//        if (firebaseAuth.getCurrentUser()!=null) {
//            Toast.makeText(LoginActivity.this, "로그인되었습니다.", Toast.LENGTH_LONG);
//            startActivity(new Intent(LoginActivity.this, MainActivity.class));
//            finish();
//        }
    }

    @Override
    public void onClick(View view) throws IllegalArgumentException{
        if(view==btn_signup){
            startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
        }
        else if(view==btn_login){
            String email = et_email.getText().toString().trim();
            String pwd = et_password.getText().toString().trim();
            try{
                firebaseAuth.signInWithEmailAndPassword(email, pwd)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                                Log.d("LoginActivity","uid="+uid);
                                Intent intent = new Intent(LoginActivity.this, ProgressActivity.class);

                                ValueEventListener eventListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            Log.d("LoginActivity","data exist");

                                            // 서버에서 정보 받아오기
//                                            ServerHelper.initServer();

                                            // token 저장: 같은 기기에서 다른 아이디를 사용할 때가 있으므로 로그인할 때마다 토큰 업데이트
                                            FirebaseMessaging.getInstance().getToken()
                                                    .addOnCompleteListener(new OnCompleteListener<String>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<String> task) {
                                                            if (!task.isSuccessful()) {
                                                                Log.w("LoginActivity", "Fetching FCM registration token failed", task.getException());
                                                                return;
                                                            }

                                                            // Get new FCM registration token
                                                            String token = task.getResult();

                                                            // Log and toast
                                                            Log.d("LoginActivity", token);
                                                            ref.child("Users").child(uid).child("token").setValue(token);
                                                        }
                                                    });

                                            startActivity(intent);
                                            finish();

                                        }else Log.e("LoginActivity","data doesnt exist");
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("LoginActivity", String.valueOf(error.toException()));
                                    }
                                };
                                ref.child("Users").child(uid).addListenerForSingleValueEvent(eventListener);
                            } else {
                                tv_login_failed.setVisibility(View.VISIBLE);
                            }
                        });
            } catch(Exception e){
                tv_login_failed.setVisibility(View.VISIBLE);
            }
        } else if(view==signInButton){
            Intent signInIntent = googleSignInClient.getSignInIntent(); //구글로그인 페이지로 가는 인텐트 객체
            startActivityForResult(signInIntent, RC_SIGNIN); //Google Sign In flow 시작
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGNIN) { //requestCode를 받은 경우
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("Google Login",  "Success:"+account.getId());
                //GoogleSignInAccount 객체에서 ID 토큰을 가져와서 firebaseAuthWithGoogle함수로 전달
                firebaseAuthWithGoogle(account.getIdToken(), account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.d("Google Login", "Google sign in failed", e);
            }
        }

    }

    private void firebaseAuthWithGoogle(String idToken, GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            uid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
                            Log.d("Google login","uid="+uid);

                            // token 저장: 같은 기기에서 다른 아이디를 사용할 때가 있으므로 로그인할 때마다 토큰 업데이트
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {
                                            if (!task.isSuccessful()) {
                                                Log.e("LoginActivity", "Fetching FCM registration token failed", task.getException());
                                                return;
                                            }

                                            // Get new FCM registration token
                                            String token = task.getResult();

                                            // Log and toast
                                            Log.d("LoginActivity", token);
                                            ref.child("Users").child(uid).child("token").setValue(token);
                                        }
                                    });

                            Intent intent = new Intent(LoginActivity.this, ProgressActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,"구글 로그인 실패",Toast.LENGTH_SHORT).show();
                            tv_login_failed.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() { //뒤로가기 했을 때
        // 두번 클릭시 종료
        if (System.currentTimeMillis() - lastTimeBackPressed < 2000) {
            Toast.makeText(this, "앱을 종료합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        lastTimeBackPressed = System.currentTimeMillis();
    }
}
