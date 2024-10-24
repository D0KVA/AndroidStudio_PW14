package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // Настройки темы приложения
    SharedPreferences themePreferences;
    SharedPreferences.Editor themeEditor;
    ImageButton themeToggleButton;

    private boolean isPlayerXTurn = true;
    private boolean isGameEnded = false;
    private boolean isPlayingWithBot = true;
    private Random randomGenerator = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themePreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        applySavedTheme();
        setContentView(R.layout.activity_main);

        themeToggleButton = findViewById(R.id.imgbtn);
        themeToggleButton.setOnClickListener(v -> switchThemeMode());

        GridLayout gameBoard = findViewById(R.id.gameGrid);
        for (int i = 0; i < gameBoard.getChildCount(); i++) {
            Button cellButton = (Button) gameBoard.getChildAt(i);
            int cellIndex = i;
            cellButton.setOnClickListener(v -> handlePlayerMove(cellButton, cellIndex));
        }

        Button restartButton = findViewById(R.id.restartBtn);
        restartButton.setOnClickListener(v -> restartGame());

        Button toggleGameModeButton = findViewById(R.id.toggleGameMode);
        toggleGameModeButton.setOnClickListener(v -> toggleGameMode());

        displayGameStats();
    }

    private void switchThemeMode() {
        boolean isNightMode = themePreferences.getBoolean("IS_NIGHT_MODE", false);
        themeEditor = themePreferences.edit();

        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            themeEditor.putBoolean("IS_NIGHT_MODE", false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            themeEditor.putBoolean("IS_NIGHT_MODE", true);
        }

        themeEditor.apply();
        updateThemeIcon();
    }

    private void updateThemeIcon() {
        if (themePreferences.getBoolean("IS_NIGHT_MODE", false)) {
            themeToggleButton.setImageResource(R.drawable.sun);  // Иконка солнца для тёмной темы
        } else {
            themeToggleButton.setImageResource(R.drawable.moon);  // Иконка луны для светлой темы
        }
    }

    private void applySavedTheme() {
        boolean isNightMode = themePreferences.getBoolean("IS_NIGHT_MODE", false);
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void toggleGameMode() {
        isPlayingWithBot = !isPlayingWithBot;
        Button toggleGameModeButton = findViewById(R.id.toggleGameMode);

        if (isPlayingWithBot) {
            toggleGameModeButton.setText("Игра с ботом");
        } else {
            toggleGameModeButton.setText("Игра без бота");
        }

        restartGame();
    }

    private void handlePlayerMove(Button cellButton, int index) {
        if (!cellButton.getText().toString().isEmpty() || isGameEnded) return;

        cellButton.setText(isPlayerXTurn ? "X" : "O");
        evaluateGameStatus();
        isPlayerXTurn = !isPlayerXTurn;

        if (!isGameEnded && !isPlayerXTurn && isPlayingWithBot) {
            performBotMove();
        }
    }

    private void performBotMove() {
        GridLayout gameBoard = findViewById(R.id.gameGrid);
        List<Button> emptyCells = new ArrayList<>();

        for (int i = 0; i < gameBoard.getChildCount(); i++) {
            Button cell = (Button) gameBoard.getChildAt(i);
            if (cell.getText().toString().isEmpty()) {
                emptyCells.add(cell);
            }
        }

        if (!emptyCells.isEmpty()) {
            Button botCell = emptyCells.get(randomGenerator.nextInt(emptyCells.size()));
            botCell.setText("O");
            evaluateGameStatus();
            isPlayerXTurn = !isPlayerXTurn;
        }
    }

    private void evaluateGameStatus() {
        String winner = determineWinner();

        if (winner != null) {
            Toast.makeText(this, "Победил " + winner, Toast.LENGTH_SHORT).show();
            updateStats(winner);
            isGameEnded = true;
        } else if (isDraw()) {
            Toast.makeText(this, "Ничья", Toast.LENGTH_SHORT).show();
            updateStats("draw");
            isGameEnded = true;
        }
    }

    private String determineWinner() {
        GridLayout gameBoard = findViewById(R.id.gameGrid);
        String[][] gameGrid = new String[3][3];

        for (int i = 0; i < gameBoard.getChildCount(); i++) {
            Button cell = (Button) gameBoard.getChildAt(i);
            gameGrid[i / 3][i % 3] = cell.getText().toString();
        }

        for (int i = 0; i < 3; i++) {
            if (!gameGrid[i][0].isEmpty() && gameGrid[i][0].equals(gameGrid[i][1]) && gameGrid[i][1].equals(gameGrid[i][2])) {
                return gameGrid[i][0];
            }
            if (!gameGrid[0][i].isEmpty() && gameGrid[0][i].equals(gameGrid[1][i]) && gameGrid[1][i].equals(gameGrid[2][i])) {
                return gameGrid[0][i];
            }
        }

        if (!gameGrid[0][0].isEmpty() && gameGrid[0][0].equals(gameGrid[1][1]) && gameGrid[1][1].equals(gameGrid[2][2])) {
            return gameGrid[0][0];
        }

        if (!gameGrid[0][2].isEmpty() && gameGrid[0][2].equals(gameGrid[1][1]) && gameGrid[1][1].equals(gameGrid[2][0])) {
            return gameGrid[0][2];
        }

        return null;
    }

    private boolean isDraw() {
        GridLayout gameBoard = findViewById(R.id.gameGrid);
        for (int i = 0; i < gameBoard.getChildCount(); i++) {
            Button cell = (Button) gameBoard.getChildAt(i);
            if (cell.getText().toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void restartGame() {
        GridLayout gameBoard = findViewById(R.id.gameGrid);
        for (int i = 0; i < gameBoard.getChildCount(); i++) {
            Button cell = (Button) gameBoard.getChildAt(i);
            cell.setText("");
        }
        isGameEnded = false;
        isPlayerXTurn = true;
    }

    private void displayGameStats() {
        SharedPreferences statsPreferences = getSharedPreferences("GameStats", MODE_PRIVATE);
        int xWins = statsPreferences.getInt("xWins", 0);
        int oWins = statsPreferences.getInt("oWins", 0);
        int draws = statsPreferences.getInt("draws", 0);

        TextView statsTextView = findViewById(R.id.statsView);
        statsTextView.setText("Крестики: " + xWins + " | Нолики: " + oWins + " | Ничья: " + draws);
    }

    private void updateStats(String winner) {
        SharedPreferences statsPreferences = getSharedPreferences("GameStats", MODE_PRIVATE);
        SharedPreferences.Editor statsEditor = statsPreferences.edit();

        int xWins = statsPreferences.getInt("xWins", 0);
        int oWins = statsPreferences.getInt("oWins", 0);
        int draws = statsPreferences.getInt("draws", 0);

        if (winner.equals("X")) {
            statsEditor.putInt("xWins", xWins + 1);
        } else if (winner.equals("O")) {
            statsEditor.putInt("oWins", oWins + 1);
        } else if (winner.equals("draw")) {
            statsEditor.putInt("draws", draws + 1);
        }

        statsEditor.apply();
        displayGameStats();
    }
}