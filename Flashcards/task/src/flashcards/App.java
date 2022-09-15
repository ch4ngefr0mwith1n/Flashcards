package flashcards;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class App {

    ConsoleLog cl = new ConsoleLog();

    private List<Flashcard> flashcardList = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    String importFile;
    String exportFile;

    public App(Optional<String> importFile, Optional<String> exportFile) {
        this.importFile = importFile.isPresent() ? importFile.get() : null;
        this.exportFile = exportFile.isPresent() ? exportFile.get() : null;
    }

    private void getHardestCard(List<Flashcard> list) {
        if (list.size() == 0) {
            String output1 = "There are no cards with errors.";
            cl.output(output1);
            return;
        }

        // najveća vrijednost za broj grešaka:
        int max = list.stream().mapToInt(Flashcard::getMistakeCount).max().getAsInt();

        // lista objekata koji sadrže maksimalnu vrijednost:
        List<Flashcard> results = list.stream()
                .filter(x -> x.getMistakeCount() == max)
                .collect(Collectors.toList());

        if (max == 0) {
            String output2 = "There are no cards with errors.";
            cl.output(output2);
        } else if (results.size() == 1) {
            String output3 = String.format("The hardest card is \"%s\". You have %d errors answering it\n",
                    results.get(0).getTerm(), results.get(0).getMistakeCount());
            cl.output(output3);
        } else {
            StringBuilder sb = new StringBuilder("The hardest cards are");

            results.forEach(x -> sb.append(" \"").append(x.getTerm()).append("\","));
            sb.deleteCharAt(sb.length() - 1);

            sb.append(". You have ").append(max).append(" errors answering them\n");

            cl.output(sb.toString());
        }
    }

    private void resetStats(List<Flashcard> list) {
        list.forEach(x -> x.setMistakeCount(0));
        String output = "Card statistics have been reset.";
        cl.output(output);
    }

    private void ask() {
        String output1 = "How many times to ask?";
        cl.output(output1);

        int n = Integer.parseInt(cl.input());

        int count = 0;
        while (count < n) {

            for (Flashcard f : flashcardList) {
                String term = f.getTerm();
                String correctAnswer = f.getDefinition();

                String output2 = String.format("Print the definition of \"%s\":\n", term);
                cl.output(output2);

                String userAnswer = cl.input();

                if (userAnswer.equalsIgnoreCase(correctAnswer)) {
                    String output3 = "Correct!";
                    cl.output(output3);
                } else {
                    if (checkIfDefinitionExists(userAnswer, flashcardList)) {

                        String output4 = String.format(
                                "Wrong. The right answer is \"%s\", but your definition is correct for \"%s\".\n",
                                correctAnswer,
                                findTermByDefinition(userAnswer, flashcardList)
                        );
                        cl.output(output4);

                    } else {
                        String output5 = String.format("Wrong. The right answer is \"%s\".\n", correctAnswer);
                        cl.output(output5);
                    }
                    f.setMistakeCount(f.getMistakeCount() + 1);
                }
                count++;
            }
        }
    }


    private void deleteCard() {
        String output1 = "Which card?";
        cl.output(output1);

        String inputTerm = cl.input();

        Optional<Flashcard> optFlashcard = findFlashcardByTerm(inputTerm);
        if (optFlashcard.isEmpty()) {
            String output2 = String.format("Can't remove \"%s\": there is no such card.\n\n", inputTerm);
            cl.output(output2);
            return;
        }

        Flashcard flashcard = optFlashcard.get();

        flashcardList.remove(flashcard);

        String output3 = "The card has been removed.\n";
        cl.output(output3);
    }

    private Optional<Flashcard> findFlashcardByTerm(String term) {
        return flashcardList.stream().filter(x -> x.getTerm().equals(term)).findFirst();
    }

    private void addCard() {

        String output1 = "The card:";
        cl.output(output1);

        String inputTerm = cl.input();

        if (checkIfTermExists(inputTerm, flashcardList)) {
            String output2 = String.format("The card \"%s\" already exists.\n", inputTerm);
            cl.output(output2);
            return;
        }

        String output3 = "The definition of the card:";
        cl.output(output3);

        String inputDefinition = cl.input();

        if (checkIfDefinitionExists(inputDefinition, flashcardList)) {
            String output4 = String.format("The definition \"%s\" already exists.\n", inputDefinition);
            cl.output(output4);
            return;
        }

        if (!checkIfTermExists(inputTerm, flashcardList) &&
                !checkIfDefinitionExists(inputDefinition, flashcardList)) {

            flashcardList.add(new Flashcard(inputTerm, inputDefinition));

            String output5 = String.format("The pair (\"%s\":\"%s\") has been added.\n", inputTerm, inputDefinition);
            cl.output(output5);
        }
    }

    private String findTermByDefinition(String definition, List<Flashcard> list) {
        return list.stream()
                .filter(x -> x.getDefinition().equals(definition))
                .findFirst()
                .get()
                .getTerm();
    }

    private boolean checkIfTermExists(String term, List<Flashcard> list){
        return list.stream()
                .anyMatch(x -> x.getTerm().equals(term));
    }

    private boolean checkIfDefinitionExists(String definition, List<Flashcard> list){
        return list.stream()
                .anyMatch(x-> x.getDefinition().equals(definition));
    }


    private void saveChanges() {
        String output1 = "File name:";
        cl.output(output1);

        File file1 = new File(cl.input());

        try(ObjectOutputStream input = new ObjectOutputStream(new FileOutputStream(file1))) {
            // upisivanje liste u fajl:
            input.writeObject(flashcardList);
        } catch (IOException ex) {
            String output2 = "Save file error";
            cl.output(output2);
        }

        String output3 = String.format("%d cards have been saved.", flashcardList.size());
        cl.output(output3);
    }

    private void loadSaves(Optional<String> loadFile) {
        String output1 = "File name:";
        cl.output(output1);

        File file2 = new File(cl.input());

        try(ObjectInputStream output = new ObjectInputStream(new FileInputStream(file2))) {
            // učitavanje iz fajla u listu:
            List<Flashcard> loadedList = (ArrayList) output.readObject();

            int counter = 0;

            // ukoliko se neka od učitanih kartica već nalazi u postojećoj listi, onda radimo Update:
            for (Flashcard f : loadedList) {
                Optional<Flashcard> optFlashcard = getFlashcardByTerm(f.getTerm());
                if (optFlashcard.isPresent()) {
                    Flashcard flashcard = optFlashcard.get();
                    flashcard.setDefinition(f.getDefinition());
                } else {
                    flashcardList.add(f);
                }
                counter++;
            }
            String output2 = String.format("%d cards have been loaded.\n", counter);
            cl.output(output2);
        } catch (IOException | ClassNotFoundException ex) {
            String output3 = "File not found.";
            cl.output(output3);
        }
    }

    Optional<Flashcard> getFlashcardByTerm(String term) {
        return flashcardList.stream().filter(x -> x.getTerm().equals(term)).findFirst();
    }

    public void start() {
        //Scanner in = new Scanner(System.in);

        for (;;) {

            String output1 = "Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):";
            cl.output(output1);

            String input = cl.input();

            switch (input) {
                case "add":
                    addCard();
                    break;
                case "remove":
                    deleteCard();
                    break;
                case "import":
                    //loadSaves();
                    break;
                case "export":
                    saveChanges();
                    break;
                case "ask":
                    ask();
                    break;
                case "log":
                    cl.saveLog();
                    break;
                case "hardest card":
                    getHardestCard(flashcardList);
                    break;
                case "reset stats":
                    resetStats(flashcardList);
                    break;
                case "exit":
                    String output2 = "Bye bye!";
                    cl.output(output2);

                    System.exit(0);
                    break;
                default:
                    String output3 = "Invalid option!";
                    cl.output(output3);
            }
        }
    }


}
