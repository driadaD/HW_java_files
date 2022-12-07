package guru.qa.model;

import lombok.Getter;

@Getter
public class NewJson {
    private String name;
    private Peoples[] peoples;

    @Getter
    public static class Peoples {
        private String name;
        private String[] object;
    }
}