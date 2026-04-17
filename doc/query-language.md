# Język zapytań JSON

## Struktura zapytania

```sql
SELECT <pola> FROM <kolekcja> [WHERE <warunek>]
```

Przykłady:

```sql
SELECT * FROM users
SELECT .name, .age FROM users
SELECT .address.city FROM orders WHERE .active == true
SELECT .name, .age FROM users WHERE .age >= 18 AND .active == true
```

## SELECT

Klauzula SELECT określa, które pola mają być zwrócone.

### Wildcard

```sql
SELECT * FROM users
```

### Lista pól

Pola są ścieżkami JSON oddzielonymi przecinkami:

```sql
SELECT .name FROM users
SELECT .name, .age, .address.city FROM users
```

## FROM

Klauzula FROM wskazuje nazwę kolekcji:

```sql
SELECT * FROM users
SELECT * FROM orders
```

## WHERE

Klauzula WHERE filtruje dokumenty na podstawie wyrażenia logicznego zbudowanego z:
- porównań,
- operatorów `EXISTS` i `IS`,
- wywołań funkcji,
- operatorów logicznych `NOT`, `AND`, `OR`,
- nawiasów.

Przykłady:

```sql
SELECT * FROM users WHERE .age == 25
SELECT * FROM users WHERE .name EXISTS
SELECT * FROM users WHERE .name IS STRING
SELECT * FROM users WHERE NOT .deletedAt EXISTS
SELECT * FROM users WHERE (.age >= 18 AND .active == true) OR .role == "admin"
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

W słowach kluczowych wielkość liter nie jest rozróżniana, tj. `and`, `AND` i `And` są traktowane tak samo.

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

## Przykłady pełnych zapytań

```sql
SELECT * FROM users WHERE .age >= 18 AND .active == true
```

```sql
SELECT .name, .role FROM users WHERE .role == "admin" OR .role == "moderator"
```

```sql
SELECT * FROM users WHERE .email EXISTS AND .email IS STRING AND NOT .deletedAt EXISTS
```

```sql
SELECT .name, .address.city FROM users WHERE .tags IS ARRAY AND contains(.tags, "admin")
```

```sql
SELECT * FROM orders WHERE (.score >= 80 AND .level IS NUMBER) OR .role == "admin"
```