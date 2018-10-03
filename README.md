# akka-java-sample

Пример взят из https://github.com/RayRoestenburg/akka-in-action
Включена java 11

#Команда httpie, посылающая запрос POST нашему серверу с единственным параметром
#создать новое событие с некоторым числом билетов
http POST localhost:5000/events/RHCP tickets:=10

#Запросить список всех событий
http GET localhost:5000/events 