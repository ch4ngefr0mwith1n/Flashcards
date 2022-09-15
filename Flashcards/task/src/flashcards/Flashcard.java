package flashcards;

import java.io.Serializable;
import java.util.Objects;

public class Flashcard implements Serializable {
    private String term;
    private String definition;
    private int mistakeCount;

    public Flashcard(String term, String definition) {
        this.term = term;
        this.definition = definition;
        this.mistakeCount = 0;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getMistakeCount() {
        return mistakeCount;
    }

    public void setMistakeCount(int mistakeCount) {
        this.mistakeCount = mistakeCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flashcard flashcard = (Flashcard) o;
        return Objects.equals(term, flashcard.term) && Objects.equals(definition, flashcard.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, definition);
    }
}
