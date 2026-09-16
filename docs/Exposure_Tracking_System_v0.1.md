# Exposure Tracking System v0.1

## Source of Truth

This document is the authoritative technical specification
for the Exposure Tracking System project.

AI assistants must follow:
- terminology defined in this document
- entity names
- metric names
- formulas
- architecture
- data structures
- MVP scope

Do not rename existing concepts without explicit approval.

If a requested implementation conflicts with this document:
1. identify the conflict
2. explain it briefly
3. do not silently change the specification

Технический документ — Exposure Tracking System v0.1
1. Назначение системы
Exposure Tracking System (ETS) — система оценки индивидуального воздействия загрязнения окружающей среды.
Система не является измерительным или медицинским прибором. Основной результат — условный Exposure Score 0–100. Количественные значения загрязнителей являются дополнительной приблизительной оценкой.

2. Базовая архитектура
USER DATA
GPS / Motion / Wearables
        ↓
MOVEMENT ENGINE
Route / Transport / Activity / Time
        ↓
ENVIRONMENT ENGINE
Air / Weather / Roads / Traffic / Industry / Noise
        ↓
EXPOSURE ENGINE
Pollution × Time × Activity × Environment
        ↓
EXPOSURE SCORE
        ↓
Map / Daily Report / History / Recommendations

3. Метрики системы мониторинга
Термин
Описание
Единица
GPS
Географическое положение
координаты
Location
Текущее местоположение
lat/lon
Distance
Пройденное расстояние
м / км
Route
Последовательность координат
координаты
Segment
Отдельный участок маршрута
м / км
Time
Время нахождения
сек / мин / ч
Duration
Продолжительность Segment
мин
Speed
Скорость движения
км/ч
Altitude
Высота
м
Heading
Направление движения
°
Accuracy
Точность GPS
м


4. Метрики передвижения
Термин
Описание
Walk
Пешее движение
Run
Бег
Bike
Велосипед
Car
Автомобиль
Bus
Автобус
Train
Поезд
Tram
Трамвай
Metro
Метро
Motorcycle
Мотоцикл
Unknown
Неопределённый тип
Stationary
Пользователь находится без движения

Activity Level
REST
WALK
FAST_WALK
RUN
BIKE
Используется для оценки интенсивности дыхания.

5. Сенсоры устройства
Термин
Источник
Использование
GPS
Телефон
положение
Accelerometer
Телефон/Watch
движение
Gyroscope
Телефон/Watch
характер движения
Barometer
Телефон/Watch
высота/контекст
Compass
Телефон
направление
Microphone
Телефон/наушники
окружающий шум
Bluetooth
Телефон
подключённые устройства
Wearables
Watch
активность
Health API
ОС
активность/движение
Headphones
AirPods/другие
доступные сенсорные данные


6. Метрики окружающей среды
Air Quality
Термин
Описание
Единица
PM2.5
Мелкие частицы
µg/m³
PM10
Частицы до 10 мкм
µg/m³
NO2
Диоксид азота
µg/m³
O3
Озон
µg/m³
SO2
Диоксид серы
µg/m³
CO
Угарный газ
µg/m³ / mg/m³
AQI
Индекс качества воздуха
0–500*

* Система должна хранить исходные концентрации, а не полагаться только на AQI, поскольку разные страны используют разные методики.

7. Погодные метрики
Термин
Описание
Единица
Temperature
Температура
°C
Humidity
Влажность
%
Wind Speed
Скорость ветра
м/с
Wind Direction
Направление ветра
°
Precipitation
Осадки
мм
Rain
Факт дождя
boolean
Rain Duration
Продолжительность дождя
мин
Time Since Rain
Время после дождя
ч
Pressure
Атмосферное давление
hPa
UV Index
Уровень UV
0–11+


8. Географические факторы
Термин
Описание
Road Distance
Расстояние до ближайшей дороги
Road Type
Тип дороги
Traffic Intensity
Интенсивность движения
Industrial Distance
Расстояние до промышленного объекта
Industrial Zone
Нахождение в промышленной зоне
Green Area
Парк/лес/зелёная зона
Building Density
Плотность застройки
Street Canyon
Улица между плотной застройкой
Elevation
Высота относительно источника
Air Station Distance
Расстояние до станции мониторинга


9. Источники загрязнения
Source Type
ROAD
INDUSTRIAL
CONSTRUCTION
PORT
AIRPORT
RESIDENTIAL
AGRICULTURAL
NATURAL
UNKNOWN
Source Distance
Расстояние:
Source → User
Source Direction
Направление:
Source → User
User → Source
Crosswind
Особенно важно для промышленного загрязнения и дорог.

10. Noise Engine
Микрофон не измеряет загрязнение напрямую.
Он определяет:
Noise Level
Noise Type
Noise Duration
Noise Type
ROAD_TRAFFIC
BUS
TRAIN
AIRCRAFT
CONSTRUCTION
INDUSTRIAL
URBAN
NATURAL
UNKNOWN
Noise используется как Environmental Modifier / Confidence Signal.
Пример:
Road Distance = 20m
Traffic = High
Noise = Road Traffic / High
→ увеличивается уверенность модели в дорожном воздействии.

11. Основная единица расчёта — Segment
Весь маршрут разбивается на сегменты.
Пример:
Segment 1:
08:00–08:10
Walk
Road Distance = 30m

Segment 2:
08:10–08:50
Bus
Road Distance = variable

Segment 3:
08:50–09:10
Walk
Industrial Zone
Каждый Segment получает собственный:
Segment Exposure Score

12. Exposure Model
Базовая формула:
E_segment =
P × T × A × R × W × S
где:
Параметр
Значение
P
Pollution Factor
T
Time Factor
A
Activity Factor
R
Road/Source Factor
W
Weather Factor
S
Source/Context Factor

Все коэффициенты нормализуются примерно в диапазон 0–1+.

13. Pollution Factor
Сначала рассчитываются отдельные загрязнители:
E_PM25
E_PM10
E_NO2
E_O3
E_SO2
E_CO
Затем:
P =
W_PM25 × E_PM25 +
W_PM10 × E_PM10 +
W_NO2 × E_NO2 +
W_O3 × E_O3 +
...
Где W — вес загрязнителя.
Важно: веса должны быть отдельными конфигурационными параметрами, чтобы впоследствии их можно было менять без переписывания приложения.

14. Time Factor
Основной принцип:
больше Duration → больше Exposure
MVP:
T = Duration / ReferenceDuration
Затем значение ограничивается нормализатором.

15. Activity Factor
Пример начальной конфигурации:
Stationary = 0.7
Walk       = 1.0
Fast Walk  = 1.2
Run        = 1.5
Bike       = 1.4
Это параметры модели, а не медицинские коэффициенты.

16. Road / Source Factor
Зависит от:
Distance to source
+
Traffic intensity
+
Source type
+
Wind direction
Пример логики:
Distance ↓
Traffic ↑
Wind from source → user
        ↓
Road Exposure ↑

17. Weather Factor
Погода изменяет условия распространения загрязнения.
Основные входы:
Wind Speed
Wind Direction
Temperature
Humidity
Rain
Time Since Rain
Pressure
Например:
Wind from source → user
+
low wind
+
no recent rain
→ потенциальное увеличение Exposure.

18. Environmental Confidence
Каждый расчёт должен иметь не только Score, но и:
Confidence Score
0–100%
Например:
Высокая уверенность
Есть:
близкая официальная станция;
актуальные данные;
GPS;
погода;
данные о дорогах.
Низкая уверенность
станция далеко;
нет traffic data;
плохой GPS;
отсутствуют данные по конкретному загрязнителю.
Это критично, чтобы система не создавала ложную точность.

19. Daily Exposure Score
Суммируем экспозицию всех сегментов:
Total Exposure =
Σ E_segment
Затем нормализуем:
Exposure Score =
100 × (1 − e^(−Total Exposure / K))
K — параметр калибровки модели.
Результат:
0–100

20. Категории Score
Начальная продуктовая шкала:
Score
Категория
0–20
LOW
21–40
MODERATE
41–60
ELEVATED
61–80
HIGH
81–100
VERY_HIGH

Пороги являются конфигурацией MVP и должны валидироваться.

21. Exposure Breakdown
Каждый день система хранит:
Total Exposure Score

PM2.5 Contribution
PM10 Contribution
NO2 Contribution
O3 Contribution
...

Road Contribution
Industrial Contribution
Transport Contribution
Other Contribution
Пример:
Daily Score: 67

PM2.5       42%
NO2         31%
PM10        18%
O3           9%

Road        54%
Transport   27%
Industrial  14%
Other        5%

22. Основные сущности системы
USER
DEVICE
LOCATION
ROUTE
SEGMENT
ACTIVITY
TRANSPORT
AIR_QUALITY
WEATHER
ROAD
TRAFFIC
SOURCE
NOISE
SENSOR
EXPOSURE
EXPOSURE_SCORE
CONFIDENCE
RECOMMENDATION

23. Главная формула продукта
В конечном итоге:
USER
+
LOCATION
+
TIME
+
ACTIVITY
+
TRANSPORT
+
AIR QUALITY
+
WEATHER
+
ROADS
+
TRAFFIC
+
INDUSTRIAL SOURCES
+
SENSORS
        ↓
EXPOSURE ENGINE
        ↓
SEGMENT EXPOSURE
        ↓
DAILY EXPOSURE
        ↓
EXPOSURE SCORE 0–100
        ↓
LEVEL + MAP + BREAKDOWN + RECOMMENDATIONS
Принцип MVP
Сначала строим детерминированную модель с понятными коэффициентами.
Не используем ML на первом этапе.
ML появляется только после накопления реальных данных и возможности проверить:
насколько рассчитанный Exposure Score соответствует фактической экспозиции.
Это и будет базовая техническая спецификация, от которой можно переходить к следующему документу: «Data Sources & APIs» — конкретно какие данные нужны, где их брать и какие API/SDK использовать для iOS и Android.

