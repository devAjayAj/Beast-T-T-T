package com.beast.tictactoe.xo;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TwoPlayerOnline extends AppCompatActivity {
    private static final String TAG = TwoPlayerOnline.class.getSimpleName();
    Boolean changeGreaterCount;
    int greaterCount = 0, player = 2;
    int[] smallCount = {0, 0, 0, 0, 0, 0, 0, 0, 0}; //no. of small boxes played in each bigger one, Eg:[3, 0, 9, 2, 9, 0, 0, 0, 0], 3 small boxes played in 1st big box
    int[] ninePlaces = { //keep track of which small box is played and by whom
            2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2
    };
    int[] threePlaces = { //which big box is won by whom, 0 ->X won, 1 ->O won, 2 ->Initial, 3 ->draw
            2, 2, 2,
            2, 2, 2,
            2, 2, 2
    };
    boolean gameOver = false, tempBlockVar = true;
    int smallImageViewIds[] = {
            R.id.nineb0, R.id.nineb1, R.id.nineb2, R.id.nineb3, R.id.nineb4, R.id.nineb5, R.id.nineb6, R.id.nineb7, R.id.nineb8, R.id.nineb9
            , R.id.nineb10, R.id.nineb11, R.id.nineb12, R.id.nineb13, R.id.nineb14, R.id.nineb15, R.id.nineb16, R.id.nineb17, R.id.nineb18
            , R.id.nineb19, R.id.nineb20, R.id.nineb21, R.id.nineb22, R.id.nineb23, R.id.nineb24, R.id.nineb25, R.id.nineb26, R.id.nineb27
            , R.id.nineb28, R.id.nineb29, R.id.nineb30, R.id.nineb31, R.id.nineb32, R.id.nineb33, R.id.nineb34, R.id.nineb35, R.id.nineb36
            , R.id.nineb37, R.id.nineb38, R.id.nineb39, R.id.nineb40, R.id.nineb41, R.id.nineb42, R.id.nineb43, R.id.nineb44, R.id.nineb45
            , R.id.nineb46, R.id.nineb47, R.id.nineb48, R.id.nineb49, R.id.nineb50, R.id.nineb51, R.id.nineb52, R.id.nineb53, R.id.nineb54
            , R.id.nineb55, R.id.nineb56, R.id.nineb57, R.id.nineb58, R.id.nineb59, R.id.nineb60, R.id.nineb61, R.id.nineb62, R.id.nineb63
            , R.id.nineb64, R.id.nineb65, R.id.nineb66, R.id.nineb67, R.id.nineb68, R.id.nineb69, R.id.nineb70, R.id.nineb71, R.id.nineb72
            , R.id.nineb73, R.id.nineb74, R.id.nineb75, R.id.nineb76, R.id.nineb77, R.id.nineb78, R.id.nineb79, R.id.nineb80
    };

    int[] bigImageViewIds = {
            R.id.nineBig0, R.id.nineBig1, R.id.nineBig2, R.id.nineBig3, R.id.nineBig4, R.id.nineBig5, R.id.nineBig6, R.id.nineBig7, R.id.nineBig8
    };
    TextView dispTurn, winner;
    Button playAgain;
    ImageView back;
    DatabaseReference mDatabase, updateChildRef;
    FirebaseUser user;
    int currentPlayer;
    String currXpos, currOpos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beast);
        blockAll();
        winner = (TextView) findViewById(R.id.winner);
        dispTurn = (TextView) findViewById(R.id.dispTurn);
        dispTurn.setVisibility(View.VISIBLE);
        playAgain = (Button) findViewById(R.id.pagain);
        playAgain.setVisibility(View.INVISIBLE);
        playAgain.setClickable(false);
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwoPlayerOnline.this.finish();
            }
        });
    }
    @Override
    public void onStart(){
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        updateChildRef = FirebaseDatabase.getInstance().getReference().child("board/" + getIntent().getStringExtra("pushKey"));
        updateChildRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Board board = dataSnapshot.getValue(Board.class);
                if (board != null && !gameOver) {
                    Log.e(TAG, "" + board.xpos);
                    Log.e(TAG, "" + board.opos);
                    Log.e(TAG, "" + board.big);
                    Log.e(TAG, "" + board.megacount);
                    Log.e(TAG, "" + board.minicount);
                    Log.e(TAG, "" + board.x);
                    Log.e(TAG, "" + board.o);
                    Log.e(TAG, "" + board.first);
                    Log.e(TAG, "" + board.cpos);
                    Log.e(TAG, "" + board.cplayer);
                    currXpos = board.xpos;
                    currOpos = board.opos;
                    threePlaces = stringToArray(board.big, 0);
                    greaterCount = board.megacount;
                    smallCount = stringToArray(board.minicount, 0);
                    setNinePlacesMatrix(stringToArray(board.xpos, 1), stringToArray(board.opos, 1));
                    if (board.first) {
                        if (board.x.equals(user.getUid())) {
                            player = 0;
                        } else {
                            player = 1;
                        }
                    }
                    if (board.cplayer == player) {
                        if (!board.first) {
                            if (player == 1) {
                                ImageView temp = (ImageView) findViewById(smallImageViewIds[board.cpos]);
                                temp.setImageResource(R.drawable.x);
                                check(board.cpos, 0, false);
                            } else if (player == 0) {
                                ImageView temp = (ImageView) findViewById(smallImageViewIds[board.cpos]);
                                temp.setImageResource(R.drawable.o);
                                check(board.cpos, 1, false);
                            }
                        }
                        SpannableString spantext = new SpannableString("Your Turn");
//                        spantext.setSpan(new RelativeSizeSpan(1.7f), 0, 4, 0);
                        dispTurn.setText(spantext, TextView.BufferType.SPANNABLE);
                        unBlockAll();
                        if (!board.first && !gameOver) {
                            block(board.cpos);
                            ImageView temp = (ImageView) findViewById(smallImageViewIds[board.cpos]);
                            if (player == 1)
                                temp.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.xclrlight));
                            else if (player == 0)
                                temp.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.oclrlight));
                        }
                    } else {
                        SpannableString spantext = new SpannableString("Opponent's Turn");
//                        spantext.setSpan(new RelativeSizeSpan(1.7f), 0, 8, 0);
                        dispTurn.setText(spantext, TextView.BufferType.SPANNABLE);
                        blockAll();
                    }
                }
                if (gameOver) {
                    blockAll();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "dberror:" + databaseError);
            }
        });
    }

    public void onclickBig(View v) {
        if (tempBlockVar) {
            Animation slideIn = new TranslateAnimation(0, 0, 0, 200);
            slideIn.setDuration(300);
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(800);
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setStartOffset(1500);
            fadeOut.setDuration(1000);
            TextView disp = (TextView) findViewById(R.id.toastCardViewText);
            if (player == 1) {
                disp.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.xclr));
            } else if (player == 0) {
                disp.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.oclr));
            }
            CardView toastCard = (CardView) findViewById(R.id.toastCardView);
            toastCard.setVisibility(View.VISIBLE);
            AnimationSet animation = new AnimationSet(false);
            animation.setFillAfter(true);
            animation.addAnimation(fadeIn);
            animation.addAnimation(slideIn);
            animation.addAnimation(fadeOut);
            toastCard.startAnimation(animation);
        }
    }

    public void onclick(View v) {
        String idName = v.getResources().getResourceEntryName(v.getId());
        String a = idName.replaceAll("[^0-9]", "");
        int idNum = Integer.parseInt(a);//current pos where player played
        ImageView temp = (ImageView) findViewById(smallImageViewIds[idNum]);
        if (ninePlaces[idNum] == 2) {
            if (player == 0) {
                temp.setImageResource(R.drawable.x);
//                player = 1;
                ninePlaces[idNum] = 0;
                smallCount[idNum / 9]++;
                SpannableString spantext = new SpannableString("O's Turn");
                spantext.setSpan(new RelativeSizeSpan(1.7f), 0, 3, 0);
                dispTurn.setText(spantext, TextView.BufferType.SPANNABLE);
            } else if (player == 1) {
                temp.setImageResource(R.drawable.o);
//                player = 0;
                ninePlaces[idNum] = 1;
                smallCount[idNum / 9]++;
                SpannableString spantext = new SpannableString("X's Turn");
                spantext.setSpan(new RelativeSizeSpan(1.7f), 0, 3, 0);
                dispTurn.setText(spantext, TextView.BufferType.SPANNABLE);
            }
            check(idNum, player, true);
            writeData(idNum);
//            if (!gameOver)
//                block(idNum);
        }
    }

    public void check(int idNum, int player, Boolean greaterCountValue) {
        changeGreaterCount = greaterCountValue;
        int smallStart = (idNum / 9) * 9;
        int bigPos = idNum / 9;
        if (ninePlaces[smallStart] == ninePlaces[smallStart + 1] && ninePlaces[smallStart + 1] == ninePlaces[smallStart + 2] && ninePlaces[smallStart] != 2) {
            displayWin(smallStart, smallStart + 1, smallStart + 2, bigPos, player, idNum);
        } else if (ninePlaces[smallStart + 3] == ninePlaces[smallStart + 4] && ninePlaces[smallStart + 4] == ninePlaces[smallStart + 5] && ninePlaces[smallStart + 3] != 2) {
            displayWin(smallStart + 3, smallStart + 4, smallStart + 5, bigPos, player, idNum);
        } else if (ninePlaces[smallStart + 6] == ninePlaces[smallStart + 7] && ninePlaces[smallStart + 7] == ninePlaces[smallStart + 8] && ninePlaces[smallStart + 8] != 2) {
            displayWin(smallStart + 6, smallStart + 7, smallStart + 8, bigPos, player, idNum);
        } else if (ninePlaces[smallStart] == ninePlaces[smallStart + 3] && ninePlaces[smallStart + 3] == ninePlaces[smallStart + 6] && ninePlaces[smallStart] != 2) {
            displayWin(smallStart, smallStart + 3, smallStart + 6, bigPos, player, idNum);
        } else if (ninePlaces[smallStart + 1] == ninePlaces[smallStart + 4] && ninePlaces[smallStart + 4] == ninePlaces[smallStart + 7] && ninePlaces[smallStart + 1] != 2) {
            displayWin(smallStart + 1, smallStart + 4, smallStart + 7, bigPos, player, idNum);
        } else if (ninePlaces[smallStart + 2] == ninePlaces[smallStart + 5] && ninePlaces[smallStart + 5] == ninePlaces[smallStart + 8] && ninePlaces[smallStart + 2] != 2) {
            displayWin(smallStart + 2, smallStart + 5, smallStart + 8, bigPos, player, idNum);
        } else if (ninePlaces[smallStart] == ninePlaces[smallStart + 4] && ninePlaces[smallStart + 4] == ninePlaces[smallStart + 8] && ninePlaces[smallStart + 4] != 2) {
            displayWin(smallStart, smallStart + 4, smallStart + 8, bigPos, player, idNum);
        } else if (ninePlaces[smallStart + 2] == ninePlaces[smallStart + 4] && ninePlaces[smallStart + 4] == ninePlaces[smallStart + 6] && ninePlaces[smallStart + 6] != 2) {
            displayWin(smallStart + 2, smallStart + 4, smallStart + 6, bigPos, player, idNum);
        } else if (smallCount[idNum / 9] == 9) {
            greaterCount++;
            threePlaces[idNum / 9] = 3;
            LinearLayout big = (LinearLayout) findViewById(bigImageViewIds[bigPos]);
            big.setBackgroundResource(R.drawable.empty);
            big.setAlpha((float) 0.9);
        }
    }

    public void displayWin(int small1, int small2, int small3, int bigPos, int player, int idNum) {
        ImageView s1 = (ImageView) findViewById(smallImageViewIds[small1]);
        ImageView s2 = (ImageView) findViewById(smallImageViewIds[small2]);
        ImageView s3 = (ImageView) findViewById(smallImageViewIds[small3]);
        LinearLayout big = (LinearLayout) findViewById(bigImageViewIds[bigPos]);
        if (player == 0) {
            s1.setImageResource(R.drawable.xwin);
            s2.setImageResource(R.drawable.xwin);
            s3.setImageResource(R.drawable.xwin);
            big.setBackgroundResource(R.drawable.x);
            big.setAlpha((float) 0.7);
            threePlaces[bigPos] = 0;
            if(changeGreaterCount)
                greaterCount++;
        } else if (player == 1) {
            s1.setImageResource(R.drawable.owin);
            s2.setImageResource(R.drawable.owin);
            s3.setImageResource(R.drawable.owin);
            big.setBackgroundResource(R.drawable.o);
            big.setAlpha((float) 0.7);
            threePlaces[bigPos] = 1;
            if(changeGreaterCount)
                greaterCount++;
        }
        checkBig();
    }

    public void checkBig() {
        if (threePlaces[0] == threePlaces[1] && threePlaces[1] == threePlaces[2] && threePlaces[0] != 2 && threePlaces[0] != 3) {
            displayWinBig(0, 1, 2);
            gameOver = true;
            blockAll();
        } else if (threePlaces[3] == threePlaces[4] && threePlaces[4] == threePlaces[5] && threePlaces[3] != 2 && threePlaces[3] != 3) {
            displayWinBig(3, 4, 5);
            gameOver = true;
            blockAll();
        } else if (threePlaces[6] == threePlaces[7] && threePlaces[7] == threePlaces[8] && threePlaces[6] != 2 && threePlaces[6] != 3) {
            displayWinBig(6, 7, 8);
            gameOver = true;
            blockAll();
        } else if (threePlaces[0] == threePlaces[3] && threePlaces[3] == threePlaces[6] && threePlaces[0] != 2 && threePlaces[0] != 3) {
            displayWinBig(0, 3, 6);
            gameOver = true;
            blockAll();
        } else if (threePlaces[1] == threePlaces[4] && threePlaces[4] == threePlaces[7] && threePlaces[1] != 2 && threePlaces[1] != 3) {
            displayWinBig(1, 4, 7);
            gameOver = true;
            blockAll();
        } else if (threePlaces[2] == threePlaces[5] && threePlaces[5] == threePlaces[8] && threePlaces[2] != 2 && threePlaces[2] != 3) {
            displayWinBig(2, 5, 8);
            gameOver = true;
            blockAll();
        } else if (threePlaces[0] == threePlaces[4] && threePlaces[4] == threePlaces[8] && threePlaces[0] != 2 && threePlaces[0] != 3) {
            displayWinBig(0, 4, 8);
            gameOver = true;
            blockAll();
        } else if (threePlaces[2] == threePlaces[4] && threePlaces[4] == threePlaces[6] && threePlaces[2] != 2 && threePlaces[2] != 3) {
            displayWinBig(2, 4, 6);
            gameOver = true;
            blockAll();
        } else if (greaterCount == 9) {
            SpannableString spantext = new SpannableString("DRAW!");
            winner.setText(spantext, TextView.BufferType.SPANNABLE);
            gameOver = true;
            blockAll();
        }
    }

    public void displayWinBig(int b1, int b2, int b3) {
        LinearLayout big1 = (LinearLayout) findViewById(bigImageViewIds[b1]);
        LinearLayout big2 = (LinearLayout) findViewById(bigImageViewIds[b2]);
        LinearLayout big3 = (LinearLayout) findViewById(bigImageViewIds[b3]);
        if (threePlaces[b1] == 0) {
            big1.setBackgroundResource(R.drawable.xwin);
            big2.setBackgroundResource(R.drawable.xwin);
            big3.setBackgroundResource(R.drawable.xwin);
            SpannableString spantext = new SpannableString("X wins!");
            spantext.setSpan(new RelativeSizeSpan(1.7f), 0, 1, 0);
            winner.setText(spantext, TextView.BufferType.SPANNABLE);
        } else if (threePlaces[b1] == 1) {
            big1.setBackgroundResource(R.drawable.owin);
            big2.setBackgroundResource(R.drawable.owin);
            big3.setBackgroundResource(R.drawable.owin);
            SpannableString spantext = new SpannableString("O wins!");
            spantext.setSpan(new RelativeSizeSpan(1.7f), 0, 1, 0);
            winner.setText(spantext, TextView.BufferType.SPANNABLE);
        }
        for (int i = 0; i < 9; i++) {
            LinearLayout temp = (LinearLayout) findViewById(bigImageViewIds[i]);
            temp.setVisibility(View.VISIBLE);
        }
    }

    public void block(int idNum) {
        int bigPos = nextBigPos(idNum);
        for (int i = 0; i < 9; i++) {
            LinearLayout temp = (LinearLayout) findViewById(bigImageViewIds[i]);
            temp.setVisibility(View.VISIBLE);
            temp.setClickable(true);
        }
        for (int i = 0; i < 81; i++) {
            ImageView v = (ImageView) findViewById(smallImageViewIds[i]);
            v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        }
        if (threePlaces[bigPos] != 2) {
            for (int i = 0; i < 81; i++) {
                if (threePlaces[i / 9] == 2) {
                    ImageView v = (ImageView) findViewById(smallImageViewIds[i]);
                    if (player == 0 && ninePlaces[i] == 2) {
                        v.setImageDrawable(null);
                        v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.xclrlight));
                    } else if (player == 1 && ninePlaces[i] == 2) {
                        v.setImageDrawable(null);
                        v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.oclrlight));
                    }
                }
            }
            for (int i = 0; i < 9; i++) {
                if (threePlaces[i] == 2) {
                    LinearLayout temp = (LinearLayout) findViewById(bigImageViewIds[i]);
                    temp.setVisibility(View.GONE);
                }
            }
        } else if (threePlaces[bigPos] == 2) {
            LinearLayout temp = (LinearLayout) findViewById(bigImageViewIds[bigPos]);
            temp.setVisibility(View.GONE);
            for (int i = bigPos * 9; i < bigPos * 9 + 9; i++) {
                ImageView v = (ImageView) findViewById(smallImageViewIds[i]);
                if (player == 0 && ninePlaces[i] == 2) {
                    v.setImageDrawable(null);
                    v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.xclrlight));
                } else if (player == 1 && ninePlaces[i] == 2) {
                    v.setImageDrawable(null);
                    v.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.oclrlight));
                }
            }
        }
    }

    public void blockAll() {
        for (int i = 0; i < 9; i++) {
            LinearLayout temp = (LinearLayout) findViewById(bigImageViewIds[i]);
            temp.setVisibility(View.VISIBLE);
            temp.setClickable(true);
        }
        for (int i = 0; i < 81; i++) {
            if (ninePlaces[i] == 2) {
                ImageView temp = (ImageView) findViewById(smallImageViewIds[i]);
//            temp.setClickable(false);
                temp.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            }
        }
//        playAgain.setVisibility(View.VISIBLE);
//        playAgain.setClickable(true);
//        dispTurn.setText(" ");
//        playAgain.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                recreate();
//            }
//        });
    }

    public void unBlockAll() {
        for (int i = 0; i < 9; i++) {
            LinearLayout temp = (LinearLayout) findViewById(bigImageViewIds[i]);
            temp.setVisibility(View.INVISIBLE);
            temp.setClickable(false);
        }
    }

    public int nextBigPos(int idNum) {
        return (idNum % 9);
    }

    public void writeData(int curPos) {
        if (player == 0) {
            currXpos = currXpos + ',' + curPos;
            Map map = new HashMap();
            map.put("cplayer", 1);
            map.put("xpos", currXpos);
            map.put("cpos", curPos);
            map.put("minicount", Arrays.toString(smallCount).replaceAll("\\[", "").replaceAll("\\]", ""));
            map.put("megacount", greaterCount);
            map.put("big", "" + Arrays.toString(threePlaces).replaceAll("\\[", "").replaceAll("\\]", ""));
            map.put("first", false);
            updateChildRef.updateChildren(map);
        } else {
            currOpos = currOpos + ',' + curPos;
            Map map = new HashMap();
            map.put("cplayer", 0);
            map.put("opos", currOpos);
            map.put("cpos", curPos);
            map.put("minicount", Arrays.toString(smallCount).replaceAll("\\[", "").replaceAll("\\]", ""));
            map.put("megacount", greaterCount);
            map.put("big", Arrays.toString(threePlaces).replaceAll("\\[", "").replaceAll("\\]", ""));
            map.put("first", false);
            updateChildRef.updateChildren(map);
        }
    }

    //    public void boardClass(String pushKey) {
//        updateChildRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Board board = dataSnapshot.getValue(Board.class);
//                if(board != null && !gameOver){
//                    Log.e(TAG, "" + board.getXpos());
//                    Log.e(TAG, "" + board.getOpos());
//                    Log.e(TAG, "" + board.getBig());
//                    Log.e(TAG, ""+ board.getMegacount());
//                    Log.e(TAG, "" + board.getMinicount());
//                    Log.e(TAG, "" + board.getX());
//                    Log.e(TAG, "" + board.getO());
//                    Log.e(TAG, "" + board.getFirst());
//                    Log.e(TAG, "" + board.getCpos());
//                    Log.e(TAG, "" + board.getCplayer());
//                    currXpos = board.xpos;
//                    currOpos = board.opos;
//                    threePlaces = stringToArray(board.big, 0);
//                    greaterCount = board.megacount;
//                    smallCount = stringToArray(board.minicount, 0);
//                    setNinePlacesMatrix(stringToArray(board.xpos, 1), stringToArray(board.opos, 1));
//                    if(board.first){
//                        if(board.x.equals(user.getUid())){
//                            player = 0;
//                        }
//                        else {
//                            player = 1;
//                        }
//                    }
//                    if(board.cplayer == player) {
//                        if(!board.first){
//                            if(player == 1){
//                                ImageView temp = (ImageView) findViewById(smallImageViewIds[board.cpos]);
//                                temp.setImageResource(R.drawable.x);
//                                check(board.cpos, 0);
//                            } else if (player == 0) {
//                                ImageView temp = (ImageView) findViewById(smallImageViewIds[board.cpos]);
//                                temp.setImageResource(R.drawable.o);
//                                check(board.cpos, 1);
//                            }
//                        }
//                        SpannableString spantext = new SpannableString("Your Turn");
////                        spantext.setSpan(new RelativeSizeSpan(1.7f), 0, 4, 0);
//                        dispTurn.setText(spantext, TextView.BufferType.SPANNABLE);
//                        unBlockAll();
//                        if(!board.first && !gameOver) {
//                            block(board.cpos);
//                            ImageView temp = (ImageView) findViewById(smallImageViewIds[board.cpos]);
//                            if(player == 1)
//                                temp.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.xclrlight));
//                            else if(player == 0)
//                                temp.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.oclrlight));
//                        }
//                    }
//                    else{
//                        SpannableString spantext = new SpannableString("Opponent's Turn");
////                        spantext.setSpan(new RelativeSizeSpan(1.7f), 0, 8, 0);
//                        dispTurn.setText(spantext, TextView.BufferType.SPANNABLE);
//                        blockAll();
//                    }
//                }
//                if(gameOver){
//                    blockAll();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e(TAG,"dberror:"+databaseError);
//            }
//        });
//    }
    public int[] stringToArray(String arr, int i) {
        String[] bigAsString = arr.replaceAll("\\s", "").split(",");
        int[] result = new int[bigAsString.length];
        for (; i < bigAsString.length; i++)
            result[i] = Integer.parseInt(bigAsString[i]);
        return result;
    }

    public void setNinePlacesMatrix(int[] xp, int[] op) {
        for (int i = 1; i < xp.length; i++) {
            ninePlaces[xp[i]] = 0;
        }
        for (int i = 1; i < op.length; i++) {
            ninePlaces[op[i]] = 1;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
}
