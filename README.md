# Java_chat

Создание javadoc

```
javadoc -d docs src/client/*.java
javadoc -d docs src/server/*.java
```

Создание jar

```
javac src/server/Server.java
jar cvfe server.jar server.Server server/*.class
```

```
javac src/client/*.java
jar cvfe client.jar client.Client client/*.class
```


Для использования приложения необходимо запустить сервер и как минимум один клиент.
На клиенте можно ввести команды:
online - получение списка онлайн пользователей
messages - история 100 последих сообщений
quit - выход
upload - загрузить файл, параметр - имя файла, находящегося в подпапке пользователя
download - скачать файл, рсположенный на сервере
Если ввеленные пользователем текст не начинается с одной и  этих команд, то он будет расссмотрен как сообщение.
