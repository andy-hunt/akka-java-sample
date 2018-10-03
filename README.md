# akka-java-sample

Пример взят из https://github.com/RayRoestenburg/akka-in-action
Включена java 11

#Команда httpie, посылающая запрос POST нашему серверу с единственным параметром
#создать новое событие с некоторым числом билетов
http POST localhost:5000/events/RHCP tickets:=10

#Запросить список всех событий
http GET localhost:5000/events

#Ещё событие
http POST localhost:5000/events/DjMadlib tickets:=15
http GET localhost:5000/events

# Покупка билета на концерт RHCP
http POST localhost:5000/events/RHCP/tickets tickets:=2


http POST localhost:5000/events/RHCP tickets:=10
http GET localhost:5000/events
http POST localhost:5000/events/DjMadlib tickets:=15
http POST localhost:5000/events/RHCP/tickets tickets:=2
