package ru.otus.cryptography.hw1;

// `@Data` в `KeyLetter` — хорошо, но подумайте, нужны ли действительно все геттеры, сеттеры, `equals`, `hashCode`, `toString`. Возможно, достаточно `@Getter` и `@Setter` для `count`, а `letter` можно сделать `final`.
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Decoder {
    private final Map<Integer, Key> keys = new HashMap<>();
// Можно использовать ArrayList для простоты доступа по индексу. В целом, старайтесь минимизировать использование HashMap, если можете использовать более простой и быстрый ArrayList. Например, так:
//private final List<Key> keys = new ArrayList<>();
//Можно использовать `List<Key>` вместо `Map<Integer, Key>`. Это упрощает код и делает его более эффективным в данном контексте.

    public void findKeys(Text ciphertext1, Text ciphertext2) {
        var xored = ciphertext1.xor(ciphertext2);
        for (int l = 0; l < xored.getLetters().size(); l++) {
            var xl = xored.getLetters().get(l);
            if (xl.isEngLetter()) {
                var xlInverted = xl.invertCase();
                var cl1 = ciphertext1.getLetter(l);
                var cl2 = ciphertext2.getLetter(l);
                addKey(l, cl1.xor(xlInverted));
                addKey(l, cl2.xor(xlInverted));
            }
        }
    }

// Вложенный цикл в findKeys не нужен. Метод `xored.getLetters()` возвращает список. Пройдясь по нему один раз, можно сразу определить, является ли буква английской, и добавить ключ. Можно избежать лишних обращений к элементам списка по индексу.
// Что произойдет, если `ciphertext1` и `ciphertext2` будут разных размеров? Добавил проверку на это.
// Имена переменных типа `xl`, `cl1`, `cl2` неинформативны. Лучше использовать более описательные имена, например, `xoredLetter`, `ciphertext1Letter`, `ciphertext2Letter` и т.д.
//    public void findKeys(Text ciphertext1, Text ciphertext2) {
//        if (ciphertext1.size() != ciphertext2.size()) {
//            throw new IllegalArgumentException("Ciphertexts must have the same size");
//        }
//        List<Text.Letter> xoredLetters = ciphertext1.xor(ciphertext2).getLetters();
//        keys.clear(); // Очищаем ключи перед новым вычислением

// keys.addAll(Collections.nCopies(xoredLetters.size(), new Key())); // Создаем ключи заранее

//        for (int i = 0; i < xoredLetters.size(); i++) {
//            Text.Letter xoredLetter = xoredLetters.get(i);
//            if (xoredLetter.isEngLetter()) {
//                Text.Letter invertedLetter = xoredLetter.invertCase();
//                keys.get(i).addLetter(ciphertext1.getLetter(i).xor(invertedLetter));
//                keys.get(i).addLetter(ciphertext2.getLetter(i).xor(invertedLetter));
//            }
//        }
//        sortKeys();
//    }
// Для `sortKeys` тоже может потребоваться небольшая модификация..
    
    public void sortKeys() {
        keys.forEach((pos, key) -> key.sortKeys());
    }

// Создание списка `keyLetters` и затем объекта `Text` - избыточно. Можно напрямую создавать `Text` объект, например используя Stream API, что будет эффективнее.
    public Text decrypt(Text ciphertext) {
        var keyLetters = new ArrayList<Text.Letter>(ciphertext.size());
        for (int i = 0; i < ciphertext.size(); i++) {
            var letter = keys.containsKey(i) ? keys.get(i).getLetter() : null;
            keyLetters.add(letter);
        }
        var key = new Text(keyLetters);
        return ciphertext.xor(key);
    }

// Проверка `!keys.containsKey(position)` выполняется каждый раз. Лучше создать `Key` объект сразу для всех позиций, а затем заполнять его. Это избавит от поиска в `HashMap` внутри `addKey`.
    private void addKey(int position, Text.Letter letter) {
        if (!keys.containsKey(position)) {
            keys.put(position, new Key());
        }
        keys.get(position).addLetter(letter);
    }

    private static class Key {
        private final Map<Text.Letter, KeyLetter> keyLettersMap = new HashMap<>();
        private final Set<KeyLetter> keyLettersTreeSet = new TreeSet<>();

        private void addLetter(Text.Letter letter) {
            if (keyLettersMap.containsKey(letter)) {
                keyLettersMap.get(letter).increment();
            } else {
                var keyLetter = new KeyLetter(letter, 1);
                keyLettersMap.put(letter, keyLetter);
            }
        }

        private void sortKeys() {
            keyLettersMap.forEach((letter, keyLetter) -> keyLettersTreeSet.add(keyLetter));
        }

// Возвращение `null` не очень удобно. Лучше предусмотреть какое-то значение, хотя бы, пробел, или лучше бросить исключение, если ключ не найден.
        private Text.Letter getLetter() {
            return keyLettersTreeSet.stream().findFirst().map(KeyLetter::getLetter).orElse(null);
        }

        @Data
        private static class KeyLetter implements Comparable<KeyLetter> {
            private final Text.Letter letter;
            private int count;

            private KeyLetter(Text.Letter letter, int count) {
                this.letter = letter;
                this.count = count;
            }

// Кажется, что логику сравнения можно упростить.
            @Override
            public int compareTo(KeyLetter o) {
                var i = (o.count) - (count);
                if (i == 0) {
                    return (int) letter.getAscii() > (int) o.letter.getAscii() ? 1 : -1;
                }
                return i;
            }

            private void increment() {
                count++;
            }
        }
    }
}
