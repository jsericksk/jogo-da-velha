package com.kproject.jogodavelha.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.kproject.jogodavelha.R;
import java.util.ArrayList;
import java.util.Random;

/*
* Autor: EricksK
* Data de Criação: 01/03/2020
*/
public class MainActivity extends AppCompatActivity {
    private TextView[] tvPositions = new TextView[9];
    private ActionBar actionBar;
    private ComputerVsComputerTask computerVsComputerTask;
    
    private boolean gameFinished;
    private boolean isHumanVsHuman;
    private boolean isComputerVsComputer;
    private int currentHumanPlayer = 1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializePositions();
        actionBar = getSupportActionBar();
        showDialogGameOptions();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_restart) {
            resetGame();
            showDialogGameOptions();
        } else if (item.getItemId() == R.id.menu_restart_match) {
            resetGame();
            if (isComputerVsComputer) {
                computerVsComputerTask = new ComputerVsComputerTask();
                computerVsComputerTask.execute();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void initializePositions() {
        tvPositions[0] = findViewById(R.id.tvPosition1);
        tvPositions[1] = findViewById(R.id.tvPosition2);
        tvPositions[2] = findViewById(R.id.tvPosition3);
        tvPositions[3] = findViewById(R.id.tvPosition4);
        tvPositions[4] = findViewById(R.id.tvPosition5);
        tvPositions[5] = findViewById(R.id.tvPosition6);
        tvPositions[6] = findViewById(R.id.tvPosition7);
        tvPositions[7] = findViewById(R.id.tvPosition8);
        tvPositions[8] = findViewById(R.id.tvPosition9);
    }
    
    public void onClickPosition(View view) {
        if (!isComputerVsComputer && !gameFinished) {
            int id = view.getId();
            if (isHumanVsHuman) {
                for (int i = 0; i < 9; i++) {
                    if (id == tvPositions[i].getId() && tvPositions[i].getText().toString().isEmpty()) {
                        tvPositions[i].setText(currentHumanPlayer == 1 ? "X" : "O");
                        tvPositions[i].setTextColor(currentHumanPlayer == 1 ? Color.BLACK : Color.RED);
                        if (isWinnerOrTied(currentHumanPlayer)) {
                            return;
                        }
                        currentHumanPlayer = currentHumanPlayer == 1 ? 2 : 1;
                        break;
                    }
                }
            } else {
                // Humano vs Computador
                for (int i = 0; i < 9; i++) {
                    if (id == tvPositions[i].getId()) {
                        if (tvPositions[i].getText().toString().isEmpty()) {
                            tvPositions[i].setText("X");
                            tvPositions[i].setTextColor(Color.BLACK);
                            if (isWinnerOrTied(1)) {
                                return;
                            }
                        } else {
                            // Evita uma jogada do computador, pois a jogada humana foi inválida
                            return;
                        }
                        break;
                    }
                }
                
                computerPlay();
            }
        }
    }
	
    private void showDialogGameOptions() {
        final String[] options = {"Humano vs Humano", "Humano vs Computador", "Computador vs Computador"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                if (position == 0) {
                    isHumanVsHuman = true;
                    isComputerVsComputer = false;
                    actionBar.setSubtitle("Modo: Humano vs Humano");
                } else if (position == 1) {
                    isHumanVsHuman = false;
                    isComputerVsComputer = false;
                    actionBar.setSubtitle("Modo: Humano vs CPU");
                } else if (position == 2) {
                    isHumanVsHuman = false;
                    isComputerVsComputer = true;
                    actionBar.setSubtitle("Modo: CPU vs CPU");
                    computerVsComputerTask = new ComputerVsComputerTask();
                    computerVsComputerTask.execute();
                }
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }
	
    private boolean isWinnerOrTied(int player) {
        // Únicas combinações de jogadas possíveis para uma vitória
        final int[][] victoryCominations = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9},
                                            {1, 4, 7}, {2, 5, 8}, {3, 6, 9},
                                            {1, 5, 9}, {3, 5, 7}};
        ArrayList<Integer> positionsWithXOrO = new ArrayList<>(6);
        for (int i = 0; i < 8; i++) {
            ArrayList<Integer> positionsOk = new ArrayList<>(6);
            for (int j = 0; j < 3; j++) {
                int indexOfTextView = victoryCominations[i][j] - 1;
                
                if (player == 1) {
                    if (tvPositions[indexOfTextView].getText().toString().equals("X")) {
                        positionsOk.add(indexOfTextView);
                    }
                } else if (player == 2) {
                    if (tvPositions[indexOfTextView].getText().toString().equals("O")) {
                        positionsOk.add(indexOfTextView);
                    }
                }
                
            }
            /*
            * Uma das combinações de jogadas para a vitória ocorreu.
            * Às vezes o jogador pode vencer com 5 posições marcadas, fazendo, então,
            * mais uma combinação acontecer e adiciondo mais 3 posições à lista. Uma das
            * posições é sempre repetida, pois já existe na lista, devido a combinação anterior
            */
            if (positionsOk.size() == 3) {
                positionsWithXOrO.addAll(positionsOk);
            }
            positionsOk.clear();
        }
		
        if (positionsWithXOrO.size() == 3 || positionsWithXOrO.size() == 6) {
            for (int i = 0; i < positionsWithXOrO.size(); i++) {
                tvPositions[positionsWithXOrO.get(i)].setTextColor(Color.GREEN);
            }
            showToast(String.format("O jogador %s venceu!", player == 1 ? "\"X\"" : "\"O\""));
            this.gameFinished = true;
            return true;
        }
        // O jogo finalizou sem um vencedor, logo, "deu velha"!
        if (isGameFinished()) {
            showToast("Deu velha! O jogo empatou.");
            return true;
        }
        
        return false;
    }
    
    // Jogada do computador no modo Humano vs Computador
    private void computerPlay() {
        if (!isGameFinished()) {
            Random random = new Random();
            while (true) {
                int position = random.nextInt(9);
                if (tvPositions[position].getText().toString().isEmpty()) {
                    tvPositions[position].setText("O");
                    tvPositions[position].setTextColor(Color.RED);
                    break;
                }
            }
            isWinnerOrTied(2);
        }
    }
	
    private boolean isGameFinished() {
        if (this.gameFinished) {
            return true;
        }
        for (int i = 0; i < 9; i++) {
            if (tvPositions[i].getText().toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }
	
    private void resetGame() {
        for (int i = 0; i < 9; i++) {
            tvPositions[i].setText("");
        }
        if (computerVsComputerTask != null) {
            computerVsComputerTask.cancel(true);
        }
        this.gameFinished = false;
        this.currentHumanPlayer = 1;
    }
	
    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
	
    /*
    * Classe utilizada para criar jogadas aleatórios (não inteligentes) de ambos os jogadores.
    * É utilizada uma AsyncTask para criar um timer antes de uma nova jogada.
    */
    private class ComputerVsComputerTask extends AsyncTask<Void, Integer, Void> {
        
        @Override
        protected Void doInBackground(Void[] param) {
            Random random = new Random();
            int player = 1;
            for (int i = 0; i < 9; i++) {
                if (!isCancelled()) {
                    int position = random.nextInt(9);
					
                    while (true) {
                        /*
                        * Verifica e depois gera uma nova jogada em outra posição,
                        * caso a anterior não seja válida 
                        */
                        if (tvPositions[position].getText().toString().isEmpty() && !isGameFinished()) {
                            break;
                        }
                        position = random.nextInt(9);
                    }
                    
                    publishProgress(player, position);
                    player = player == 1 ? 2 : 1;
                    // Um pequeno timer antes da próxima jogada
                    SystemClock.sleep(1300);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer[] values) {
            super.onProgressUpdate(values);
            int player = values[0];
            int position = values[1];
            tvPositions[position].setText(player == 1 ? "X" : "O");
            tvPositions[position].setTextColor(player == 1 ? Color.BLACK : Color.RED);
            if (isWinnerOrTied(player)) {
                computerVsComputerTask = null;
                cancel(true);
            }
        }
        
    }
	
}
