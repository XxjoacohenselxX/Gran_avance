package ar.edu.et7;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private String title;
    private String category;
    private String stimulus;
    private String prompt;
    private List<Choice> choices;
    private int points;
    private List<List<String>> answers; // Respuestas correctas
    private List<String> selectedAnswers; // Respuestas seleccionadas por el estudiante

    // Getters y Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStimulus() {
        return stimulus;
    }

    public void setStimulus(String stimulus) {
        this.stimulus = stimulus;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public List<List<String>> getAnswers() {
        return answers;
    }

    public void setAnswers(List<List<String>> answers) {
        this.answers = answers;
    }

    public List<String> getSelectedAnswers() {
        return selectedAnswers;
    }

    public void setSelectedAnswers(List<String> selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }

    // Método para verificar si la respuesta seleccionada es correcta
    public boolean isCorrect() {
        if (selectedAnswers == null || selectedAnswers.isEmpty()) {
            return false; // Ninguna respuesta seleccionada
        }

        // Verifica si todas las respuestas correctas han sido seleccionadas
        for (List<String> correctAnswer : answers) {
            if (selectedAnswers.containsAll(correctAnswer) && selectedAnswers.size() == correctAnswer.size()) {
                return true; // Todas las respuestas correctas han sido seleccionadas
            }
        }
        return false; // Respuesta incorrecta
    }

    // Clase interna para representar una opción de respuesta
    public static class Choice {
        private String id;
        private String content;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
