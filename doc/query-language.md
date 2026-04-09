# Język zapytań JSON

Ten dokument opisuje aktualną składnię wspieraną przez lexer i parser.

## Ogólna postać zapytania

Zapytanie jest wyrażeniem logicznym zbudowanym z:
- porównań,
- operatorów `EXISTS` i `IS`,
- wywołań funkcji,
- operatorów logicznych `NOT`, `AND`, `OR`,
- nawiasów.

Przykłady:

```sql
.age == 25
.name EXISTS
.name IS STRING
contains(.tags, "admin")
NOT .deletedAt EXISTS
(.age >= 18 AND .active == true) OR .role == "admin"
```

## Ścieżki JSON

Ścieżka JSON:
- zawsze zaczyna się od `.`,
- składa się z segmentów rozdzielonych kropkami,
- segment może zawierać litery, cyfry i `_`.

Poprawne przykłady:

```sql
.name
.address.city
._id
.user_1.email
```

Aktualne ograniczenia:
- brak składni indeksowania tablic, np. `.items[0]`,
- brak składni z wildcardami.

## Literały

Wspierane literały:
- string,
- number,
- boolean,
- null.

Przykłady:

```sql
"John"
42
-3.14
true
false
null
```

Słowa kluczowe są nierozróżniające wielkości liter, więc `and`, `AND` i `And` są traktowane tak samo.

## Operatory porównania

Porównanie ma postać:

```sql
.path OPERATOR literal
```

Przykłady:

```sql
.age == 25
.name != "John"
.score > 80
.price >= 19.99
.level < 10
.version <= 3
.active == true
.deletedAt == null
```

| Operator | Znaczenie |
|----------|-----------|
| `==` | równe |
| `!=` | różne |
| `>` | większe |
| `>=` | większe lub równe |
| `<` | mniejsze |
| `<=` | mniejsze lub równe |

## Słowa kluczowe

### EXISTS

Sprawdza, czy ścieżka istnieje.

```sql
.name EXISTS
.address.city EXISTS
NOT .deletedAt EXISTS
```

Uwaga:
- `NOT` jest operatorem prefiksowym,
- aktualna składnia to `NOT .name EXISTS`, a nie `.name NOT EXISTS`.

### IS

Sprawdza typ wartości pod wskazaną ścieżką.

```sql
.name IS STRING
.age IS NUMBER
.active IS BOOLEAN
.tags IS ARRAY
.address IS OBJECT
.deletedAt IS NULL
```

| Typ | Opis |
|-----|------|
| `STRING` | wartość tekstowa |
| `NUMBER` | liczba całkowita lub zmiennoprzecinkowa |
| `BOOLEAN` | `true` lub `false` |
| `ARRAY` | tablica |
| `OBJECT` | obiekt |
| `NULL` | wartość `null` |

## Funkcje

Wywołanie funkcji ma postać:

```sql
functionName(arg1, arg2, ...)
```

Argumentami mogą być:
- ścieżki JSON,
- literały.

Przykłady poprawnej składni:

```sql
contains(.tags, "admin")
startsWith(.name, "Jo")
matches(.code, "[0-9]+")
isEmpty()
```

Aktualne ograniczenie parsera:
- wywołanie funkcji jest samodzielnym wyrażeniem,
- parser nie wspiera jeszcze składni typu `size(.tags) > 3`.

## Logika

Przykłady:

```sql
.name EXISTS AND .age > 25
.role == "admin" OR .role == "moderator"
NOT .name EXISTS
(.age > 18 AND .active == true) OR .role == "admin"
```

Pierwszeństwo operatorów:
1. `NOT`
2. `AND`
3. `OR`

## Zagnieżdżone pola

```sql
.address.city == "Warsaw"
.address.zip EXISTS
.contact.phone IS STRING
contains(.address.tags, "home")
```

## Przykłady pełnych zapytań

```sql
.age >= 18 AND .active == true
```

```sql
.role == "admin" OR .role == "moderator"
```

```sql
.email EXISTS AND .email IS STRING AND NOT .deletedAt EXISTS
```

```sql
.tags IS ARRAY AND contains(.tags, "admin")
```

```sql
(.score >= 80 AND .level IS NUMBER) OR .role == "admin"
```
