# Język zapytań JSON

## Operatory porównania

```sql
age == 25
name != "John"
age > 25
age >= 25
age < 25
age <= 25
```

| Operator | Znaczenie      |
|----------|----------------|
| `==`     | równe          |
| `!=`     | różne          |
| `>`      | większe        |
| `>=`     | większe równe  |
| `<`      | mniejsze       |
| `<=`     | mniejsze równe |

## Słowa kluczowe

### EXISTS

Czy pole istnieje.

```sql
name EXISTS
address.city EXISTS
name NOT EXISTS
```

### IS

Czy pole jest określonego typu.

```sql
name IS STRING
age IS NUMBER
active IS BOOLEAN
tags IS ARRAY
address IS OBJECT
value IS NULL
```

| Typ       | Opis                         |
|-----------|------------------------------|
| `STRING`  | wartość tekstowa             |
| `NUMBER`  | liczba (całkowita lub float) |
| `BOOLEAN` | `true` lub `false`           |
| `ARRAY`   | tablica                      |
| `OBJECT`  | obiekt zagnieżdżony          |
| `NULL`    | wartość `null`               |

## Funkcje

Używane gdy operacja wymaga dodatkowych argumentów.

```sql
contains(tags, "admin")
size(tags) > 3
startsWith(name, "Jo")
matches(name, "[0-9]+")
```

## Logika

```sql
name EXISTS AND age > 25
role == "admin" OR role == "moderator"
NOT name EXISTS
(age > 18 AND active == true) OR role == "admin"
```

Pierwszeństwo:
1. `NOT`
2. `AND`
3. `OR`

## Zagnieżdżone pola

```sql
address.city == "Warsaw"
address.zip EXISTS
contact.phone IS STRING
contains(address.tags, "home")
```

## Przykłady

```sql
age >= 18 AND active == true
```

```sql
role == "admin" OR role == "moderator"
```

```sql
email EXISTS AND email IS STRING AND NOT deletedAt EXISTS
```

```sql
tags IS ARRAY AND contains(tags, "admin") AND size(tags) > 1
```

```sql
(score >= 80 AND level IS NUMBER) OR role == "admin"
```
