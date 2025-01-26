package ru.otus.cryptography.hw1;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Text {

    public Text(List<Letter> letters) {
        
    }

    
    
    private final List<Letter> letters = new ArrayList<>();

    public Text(List<Letter> letters) {
        this.letters.addAll(letters);
//      this.letters = List.copyOf(letters); // можно использовать немодифицируемый список, лучше использовать `List.copyOf` для создания немодифицируемого списка в `Text`
    }

    public static Text fromHex(String hex) {
        var text = new Text(new ArrayList<>());
        for (var i = 0; i < hex.length(); i = i + 2) {
            text.letters.add(Letter.fromHex(hex.substring(i, i + 2)));
        }
        return text;
    }

// В изначальном варианте отсутствует обработка ошибок в `Letter.fromHex` и `Letter.fromAscii` в случае некорректного входного формата.
// Вариант с использованием Stream API, обработка hex-строк в `Text.fromHex` станет более надежной
//    public static Text fromHex(String hex) {
//        return new Text(hex.chars()
//                .filter(c -> c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F') // проверка на корректность HEX
//                .mapToObj(c -> Letter.fromHex(Character.toString((char)c)))
//                .collect(Collectors.toList()));
//    }

// Использование циклов `for` для обработки строк (в fromHex и в fromAscii) не очень эффективно. Для больших строк лучше использовать `String.chars()` или библиотеку Guava для более быстрой обработки.
    
    public static Text fromAscii(String ascii) {
        var text = new Text(new ArrayList<>());
        for (var i = 0; i < ascii.length(); i++) {
            text.letters.add(Letter.fromAscii(ascii.charAt(i)));
        }
        return text;
    }

// Использование `Math.min` - это хорошая идея для защиты от выходов за границы массива. Однако, можно сделать это более элегантно, например, используя Streams и `limit()`.
    public Text xor(Text key) {
        var result = new ArrayList<Letter>();
        for (int i = 0; i < Math.min(letters.size(), key.letters.size()); i++) {
            result.add(letters.get(i).xor(key.letters.get(i)));
        }
        return new Text(result);
    }

//    public Text xor(Text key) {
//            return new Text(IntStream.range(0, Math.min(letters.size(), key.letters.size()))
//                    .mapToObj(i -> letters.get(i).xor(key.letters.get(i)))
//                    .collect(Collectors.toList()));
//        }
//    }
    
    public String printHex() {
        return letters.stream().map(Letter::getHex).collect(Collectors.joining());
    }

    public String printAscii() {
        return letters.stream().map(Letter::getStr).collect(Collectors.joining());
    }

    public Letter getLetter(int i) {
        return letters.get(i);
    }

    public int size() {
        return letters.size();
    }

    @Getter
    @EqualsAndHashCode
    public static class Letter {
        private final String hex;
        private final char ascii;
        private final String str;

        public Letter(String hex, char ascii) {
            this.hex = hex;
            this.ascii = ascii;
            this.str = String.valueOf(ascii);
        }

// Функции `hexToAscii` и также `asciiToHex` можно сделать статическими методами класса `Letter` и убрать из них неявную зависимость от `String.format`, сделав более универсальные методы.
// И переменные в этих методах можно именовать более информативно.
        public static Letter fromHex(String hex) {
            return new Letter(hex, hexToAscii(hex));
        }

// добавим обработку исключений в `Letter.fromHex`:
        
//        public static Letter fromHex(String hex) {
//            try {
//                return new Letter(hex, (char) Integer.parseInt(hex, 16));
//            } catch (NumberFormatException e) {
//                throw new IllegalArgumentException("Invalid hex string: " + hex, e);
//            }
//        }
        
        public static Letter fromAscii(char ascii) {
            return new Letter(asciiToHex(ascii), ascii);
        }

//        public static Letter fromAscii(char ascii) {
//            return new Letter(String.format("%02x", (int) ascii), ascii);
//        }
        
        private static char hexToAscii(String hex) {
            // "7a" to "z"
            return (char)Integer.parseInt(hex, 16);
        }

        private static String asciiToHex(char ascii) {
            // "z" to "7a"
            return String.format("%02x", (int) ascii);
        }

        public Letter xor(Letter key) {
            if (key == null) {
                return fromAscii(ascii);
            }
            return fromAscii((char) ((byte) ascii ^ (byte) key.ascii));
        }

// Проверка на `null` в `Letter.xor` корректна, но можно указать более конкретную причину, добавив исключение, если `key` - null.
//        public Letter xor(Letter key) {
//            if (key == null) {
//                throw new IllegalArgumentException("Key cannot be null");
//            }
//            return fromAscii((char) ((byte) ascii ^ (byte) key.ascii));
//        }        
        
        public boolean isUpperCase() {
            return String.valueOf(ascii).matches("[A-Z]");
        }

// Регулярные выражения не оптимальны для проверки одного символа. Лучше использовать методы `Character.isUpperCase` и `Character.isLetter`.
// Заменим регулярные выражения на более эффективные методы
//        public boolean isUpperCase() {
//            return Character.isUpperCase(ascii);
//        }        
        
        public boolean isEngLetter() {
            return str.matches("[a-zA-Z]");
        }

// Заменим регулярные выражения на более эффективные методы
//        public boolean isEngLetter() {
//            return Character.isLetter(ascii);
//        }
        
        public Letter toUpperCase() {
            return Text.Letter.fromAscii(str.toUpperCase().charAt(0));
        }

        public Letter toLowerCase() {
            return Text.Letter.fromAscii(str.toLowerCase().charAt(0));
        }

        public Letter invertCase() {
            return isUpperCase() ? toLowerCase() : toUpperCase();
        }

        @Override
        public String toString() {
            return str;
        }
    }
}
