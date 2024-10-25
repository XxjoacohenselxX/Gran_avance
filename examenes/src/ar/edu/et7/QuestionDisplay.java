package ar.edu.et7; 
import javax.swing.*; 
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class QuestionDisplay extends JFrame {
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private JLabel titleLabel;
    private JTextArea promptArea;
    private JCheckBox[] checkboxButtons;
    private JButton nextButton;
    private Timer timer;
    private JLabel timerLabel;
    private int timeRemaining = 30 * 60; // 30 minutos en segundos
    private int score = 0;
    private int examCount = 0; // Contador de exámenes
    private JProgressBar progressBar; // Barra de progreso
    private JLabel questionCountLabel; // Etiqueta para contar las preguntas

    public QuestionDisplay(List<Question> questions) {
        this.questions = questions;
        Collections.shuffle(this.questions); // Mezcla las preguntas

        // Configuración de la ventana
        setTitle("Multiple Choice Quiz");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(0, 1));

        // Inicialización de componentes
        titleLabel = new JLabel();
        promptArea = new JTextArea();
        promptArea.setEditable(false);

        // Añadir componentes al contenedor
        add(titleLabel);
        add(promptArea);

        // Inicialización de botones de opción
        checkboxButtons = new JCheckBox[4];
        for (int i = 0; i < checkboxButtons.length; i++) {
            checkboxButtons[i] = new JCheckBox();
            add(checkboxButtons[i]);
        }

        // Botón para pasar a la siguiente pregunta
        nextButton = new JButton("Siguiente");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAnswer();
                showNextQuestion();
            }
        });
        add(nextButton);

        // Inicializar y añadir el temporizador
        timerLabel = new JLabel();
        add(timerLabel);

        // Iniciar el temporizador
        startTimer();

        // Inicializar la barra de progreso
        progressBar = new JProgressBar(0, questions.size());
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        add(progressBar); // Añadir la barra de progreso

        // Inicializar la etiqueta de conteo de preguntas
        questionCountLabel = new JLabel();
        add(questionCountLabel); // Añadir la etiqueta al contenedor

        // Mostrar la primera pregunta
        showQuestion();
    }

    private void showQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question q = questions.get(currentQuestionIndex);
            titleLabel.setText("Título: " + q.getTitle());
            promptArea.setText(q.getPrompt());

            // Verificar el tamaño de choices y asegurarse de que no haya más opciones que botones
            if (q.getChoices().size() > checkboxButtons.length) {
                throw new IllegalStateException("Más opciones que botones de elección disponibles");
            }

            // Limpiar y actualizar los botones de opción
            for (int i = 0; i < checkboxButtons.length; i++) {
                if (i < q.getChoices().size()) {
                    Question.Choice choice = q.getChoices().get(i);
                    checkboxButtons[i].setText(choice.getContent());
                    checkboxButtons[i].setActionCommand(choice.getId());
                } else {
                    checkboxButtons[i].setText(""); // Limpiar botones no utilizados
                }
            }

            // Deseleccionar todos los botones
            for (JCheckBox button : checkboxButtons) {
                button.setSelected(false);
            }
            progressBar.setValue(currentQuestionIndex + 1); // Actualizar barra de progreso
            questionCountLabel.setText("Pregunta " + (currentQuestionIndex + 1) + " de " + questions.size()); // Actualizar conteo
            currentQuestionIndex++;
        } else {
            endQuiz();
        }
    }

    private void checkAnswer() {
        if (currentQuestionIndex > 0) {
            Question q = questions.get(currentQuestionIndex - 1);
            List<String> selectedAnswers = getSelectedChoiceIds(); // Guardar respuestas seleccionadas
            q.setSelectedAnswers(selectedAnswers); // Cambiado para guardar una lista

            int earnedPoints = calculateScore(q);
            score += earnedPoints; // Solo sumar el puntaje si es positivo

            // Informar al usuario si hubo penalización
            if (earnedPoints < 0) {
                JOptionPane.showMessageDialog(this, "Has sido penalizado por 10 puntos.", "Penalización", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private List<String> getSelectedChoiceIds() {
        List<String> selectedIds = new java.util.ArrayList<>();
        for (JCheckBox button : checkboxButtons) {
            if (button.isSelected()) {
                selectedIds.add(button.getActionCommand());
            }
        }
        return selectedIds;
    }

    private void showNextQuestion() {
        showQuestion();
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeRemaining--;
                int minutes = timeRemaining / 60;
                int seconds = timeRemaining % 60;
                timerLabel.setText(String.format("Tiempo restante: %02d:%02d", minutes, seconds));
                if (timeRemaining <= 0) {
                    timer.cancel();
                    endQuiz();
                }
            }
        }, 0, 1000);
    }

    private void endQuiz() {
        examCount++; // Incrementar el contador de exámenes
        saveResultsToFile(); // Guardar resultados en el archivo
        showScoreReport();
        System.exit(0);
    }

    private void saveResultsToFile() {
        String filename = "resultado" + examCount + ".txt"; // Crear el nombre del archivo con el contador
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(String.format("Examen %d: Puntuación total: %d%n", examCount, score)); // Encabezado
            for (Question question : questions) {
                writer.write(String.format("Pregunta: %s%n", question.getTitle()));
                writer.write(String.format("Respuestas seleccionadas: %s%n", question.getSelectedAnswers() != null ? question.getSelectedAnswers() : "Ninguna"));
                writer.write(String.format("Resultado: %s%n%n", question.isCorrect() ? "Correcta" : "Incorrecta"));
            }
            writer.write("========================================\n"); // Separador entre exámenes
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private int calculateScore(Question question) {
        int scoreForQuestion = 0;
        List<String> correctAnswers = question.getAnswers().get(0); // Suponiendo que la primera lista de respuestas contiene todas las respuestas correctas

        // Obtener respuestas seleccionadas
        List<String> selectedAnswers = getSelectedChoiceIds();
        int selectedCount = selectedAnswers.size();

        // Penalización por no seleccionar ninguna respuesta o seleccionar más de dos
        // if (selectedCount == 0 || selectedCount > correctAnswers.size()) {
        //    return -10; // Penalización de 10 puntos
        //}

        // Verificar si se seleccionaron todas las respuestas correctas
        boolean hasAllCorrectAnswers = selectedAnswers.containsAll(correctAnswers) && selectedCount == correctAnswers.size();

        // Sumar puntos solo si hay respuestas correctas seleccionadas
        if (hasAllCorrectAnswers) {
            scoreForQuestion += 10; // Sumar 10 puntos por respuestas correctas
        }

        return scoreForQuestion; // Retornar el puntaje total por la pregunta
    }

    private void showScoreReport() {
        StringBuilder report = new StringBuilder();
        report.append("Reporte de Examen:\n");
        report.append(String.format("Examen %d: Puntuación Total: %d\n\n", examCount, score)); // Incluir el número de examen y la puntuación

        for (Question question : questions) {
            report.append("Pregunta: ").append(question.getTitle()).append("\n");
            report.append("Respuestas Seleccionadas: ").append(question.getSelectedAnswers() != null ? question.getSelectedAnswers() : "Ninguna").append("\n");

            // Verificar si las respuestas seleccionadas son correctas
            List<String> selectedAnswers = question.getSelectedAnswers(); // Asegúrate de que esto es una lista
            List<String> correctAnswers = question.getAnswers().get(0); // Las respuestas correctas son la primera lista

            boolean isCorrect = correctAnswers.containsAll(selectedAnswers) && selectedAnswers.containsAll(correctAnswers);

            if (isCorrect) {
                report.append("Resultado: Correcta\n");
            } else {
                report.append("Resultado: Incorrecta\n");
                report.append("Respuestas Correctas: ").append(correctAnswers).append("\n");
            }
            report.append("\n");
        }

        JOptionPane.showMessageDialog(this, report.toString(), "Reporte de Examen", JOptionPane.INFORMATION_MESSAGE);
    }
    
}