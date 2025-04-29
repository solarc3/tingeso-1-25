-- Use CURRENT_DATE (without parentheses) + specific times to set reservation times

-- Reservation 1: Today at 14:30 (2 people)
INSERT INTO reserva (
    start_time, end_time, laps, duration, num_people,
    responsible_name, responsible_email, status,
    total_price, discount_group, discount_freq, discount_birthday
)
VALUES (
                   CURRENT_DATE + TIME '14:30:00', CURRENT_DATE + TIME '15:00:00',
                   10, 30, 2, 'Juan Pérez', 'juan@example.com', 'CONFIRMED',
                   17850, 0, 0, 0
       );

-- Asignar karts
INSERT INTO reserva_karts (reserva_id, kart_id) VALUES
                                                    (1, 'K001'),
                                                    (1, 'K002');

-- Agregar invitados
INSERT INTO reserva_guests (reserva_id, name, email) VALUES
                                                         (1, 'Juan Pérez', 'juan@example.com'),
                                                         (1, 'Ana Pérez', 'ana@example.com');

-- Grupo 2: María González - 3 personas - Today at 16:00
INSERT INTO reserva (
    start_time, end_time, laps, duration, num_people,
    responsible_name, responsible_email, status,
    total_price, discount_group, discount_freq, discount_birthday
)
VALUES (
                   CURRENT_DATE + TIME '16:00:00', CURRENT_DATE + TIME '16:35:00',
                   15, 35, 3, 'María González', 'maria@example.com', 'CONFIRMED',
                   21420, 2000, 0, 0
       );

-- Asignar karts
INSERT INTO reserva_karts (reserva_id, kart_id) VALUES
                                                    (2, 'K003'),
                                                    (2, 'K004'),
                                                    (2, 'K005');

-- Agregar invitados
INSERT INTO reserva_guests (reserva_id, name, email) VALUES
                                                         (2, 'María González', 'maria@example.com'),
                                                         (2, 'Pedro López', 'pedro@example.com'),
                                                         (2, 'Luis García', 'luis@example.com');

-- Grupo 3: Carlos Rodríguez - 5 personas - Today at 18:00
INSERT INTO reserva (
    start_time, end_time, laps, duration, num_people,
    responsible_name, responsible_email, status,
    total_price, discount_group, discount_freq, discount_birthday
)
VALUES (
                   CURRENT_DATE + TIME '18:00:00', CURRENT_DATE + TIME '18:40:00',
                   20, 40, 5, 'Carlos Rodríguez', 'carlos@example.com', 'CONFIRMED',
                   23800, 5000, 0, 0
       );

-- Asignar karts
INSERT INTO reserva_karts (reserva_id, kart_id) VALUES
                                                    (3, 'K006'),
                                                    (3, 'K007'),
                                                    (3, 'K008'),
                                                    (3, 'K009'),
                                                    (3, 'K010');

-- Agregar invitados
INSERT INTO reserva_guests (reserva_id, name, email) VALUES
                                                         (3, 'Carlos Rodríguez', 'carlos@example.com'),
                                                         (3, 'Paula Martínez', 'paula@example.com'),
                                                         (3, 'Diego Toro', 'diego@example.com'),
                                                         (3, 'Carla Ruiz', 'carla@example.com'),
                                                         (3, 'Raúl Gómez', 'raul@example.com');

-- Grupo 4: Ana Martínez - 2 personas - Tomorrow at 15:30
INSERT INTO reserva (
    start_time, end_time, laps, duration, num_people,
    responsible_name, responsible_email, status,
    total_price, discount_group, discount_freq, discount_birthday
)
VALUES (
                   CURRENT_DATE + INTERVAL '1 DAY' + TIME '15:30:00', CURRENT_DATE + INTERVAL '1 DAY' + TIME '16:00:00',
                   10, 30, 2, 'Ana Martínez', 'ana.martinez@example.com', 'CONFIRMED',
                   16065, 1500, 0, 0
       );

-- Asignar karts
INSERT INTO reserva_karts (reserva_id, kart_id) VALUES
                                                    (4, 'K001'),
                                                    (4, 'K002');

-- Agregar invitados
INSERT INTO reserva_guests (reserva_id, name, email) VALUES
                                                         (4, 'Ana Martínez', 'ana.martinez@example.com'),
                                                         (4, 'Miguel Castro', 'miguel@example.com');

-- Grupo 5: Roberto Sánchez - 4 personas - Tomorrow at 17:30
INSERT INTO reserva (
    start_time, end_time, laps, duration, num_people,
    responsible_name, responsible_email, status,
    total_price, discount_group, discount_freq, discount_birthday
)
VALUES (
                   CURRENT_DATE + INTERVAL '1 DAY' + TIME '17:30:00', CURRENT_DATE + INTERVAL '1 DAY' + TIME '18:05:00',
                   15, 35, 4, 'Roberto Sánchez', 'roberto@example.com', 'CONFIRMED',
                   19040, 4000, 0, 0
       );

-- Asignar karts
INSERT INTO reserva_karts (reserva_id, kart_id) VALUES
                                                    (5, 'K003'),
                                                    (5, 'K004'),
                                                    (5, 'K005'),
                                                    (5, 'K006');

-- Agregar invitados
INSERT INTO reserva_guests (reserva_id, name, email) VALUES
                                                         (5, 'Roberto Sánchez', 'roberto@example.com'),
                                                         (5, 'Laura Vega', 'laura@example.com'),
                                                         (5, 'Carmen Díaz', 'carmen@example.com'),
                                                         (5, 'Sergio Mora', 'sergio@example.com');

-- Grupo 6: Felipe Castro - 12 personas (con descuento de cumpleaños) - 5 days ahead at 19:00
INSERT INTO reserva (
    start_time, end_time, laps, duration, num_people,
    responsible_name, responsible_email, status,
    total_price, discount_group, discount_freq, discount_birthday
)
VALUES (
                   CURRENT_DATE + INTERVAL '5 DAY' + TIME '19:00:00', CURRENT_DATE + INTERVAL '5 DAY' + TIME '19:30:00',
                   10, 30, 12, 'Felipe Castro', 'felipe@example.com', 'CONFIRMED',
                   11900, 3000, 0, 7500
       );

-- Asignar karts
INSERT INTO reserva_karts (reserva_id, kart_id) VALUES
                                                    (6, 'K001'), (6, 'K002'), (6, 'K003'), (6, 'K004'),
                                                    (6, 'K005'), (6, 'K006'), (6, 'K007'), (6, 'K008'),
                                                    (6, 'K009'), (6, 'K010'), (6, 'K011'), (6, 'K012');

-- Agregar invitados (1 responsable + 11 invitados)
INSERT INTO reserva_guests (reserva_id, name, email) VALUES
                                                         (6, 'Felipe Castro', 'felipe@example.com'),
                                                         (6, 'Invitado 1', 'inv1@example.com'),
                                                         (6, 'Invitado 2', 'inv2@example.com'),
                                                         (6, 'Invitado 3', 'inv3@example.com'),
                                                         (6, 'Invitado 4', 'inv4@example.com'),
                                                         (6, 'Invitado 5', 'inv5@example.com'),
                                                         (6, 'Invitado 6', 'inv6@example.com'),
                                                         (6, 'Invitado 7', 'inv7@example.com'),
                                                         (6, 'Invitado 8', 'inv8@example.com'),
                                                         (6, 'Invitado 9', 'inv9@example.com'),
                                                         (6, 'Invitado 10', 'inv10@example.com'),
                                                         (6, 'Invitado 11', 'inv11@example.com');

-- Grupo 7: Lucía Morales - cliente frecuente - Weekend date (+7 days) at 16:30
INSERT INTO reserva (
    start_time, end_time, laps, duration, num_people,
    responsible_name, responsible_email, status,
    total_price, discount_group, discount_freq, discount_birthday
)
VALUES (
                   CURRENT_DATE + INTERVAL '7 DAY' + TIME '16:30:00', CURRENT_DATE + INTERVAL '7 DAY' + TIME '17:10:00',
                   20, 40, 1, 'Lucía Morales', 'lucia@example.com', 'CONFIRMED',
                   20187.5, 0, 5000, 0
       );

-- Asignar karts
INSERT INTO reserva_karts (reserva_id, kart_id) VALUES
    (7, 'K001');

-- Agregar invitados
INSERT INTO reserva_guests (reserva_id, name, email) VALUES
    (7, 'Lucía Morales', 'lucia@example.com');

-- Grupo 8: Diego Toro - 2 personas - 8 days ahead at 17:30 (CANCELLED)
INSERT INTO reserva (
    start_time, end_time, laps, duration, num_people,
    responsible_name, responsible_email, status,
    total_price, discount_group, discount_freq, discount_birthday
)
VALUES (
                   CURRENT_DATE + INTERVAL '8 DAY' + TIME '17:30:00', CURRENT_DATE + INTERVAL '8 DAY' + TIME '18:00:00',
                   10, 30, 2, 'Diego Toro', 'diego2@example.com', 'CANCELLED',
                   16065, 1500, 0, 0
       );

-- Asignar karts
INSERT INTO reserva_karts (reserva_id, kart_id) VALUES
                                                    (8, 'K005'),
                                                    (8, 'K006');

-- Agregar invitados
INSERT INTO reserva_guests (reserva_id, name, email) VALUES
                                                         (8, 'Diego Toro', 'diego2@example.com'),
                                                         (8, 'Isabel López', 'isabel@example.com');

-- Grupo 9: Valentina López - próximo mes (30 days ahead) at 18:30
INSERT INTO reserva (
    start_time, end_time, laps, duration, num_people,
    responsible_name, responsible_email, status,
    total_price, discount_group, discount_freq, discount_birthday
)
VALUES (
                   CURRENT_DATE + INTERVAL '30 DAY' + TIME '18:30:00', CURRENT_DATE + INTERVAL '30 DAY' + TIME '19:05:00',
                   15, 35, 6, 'Valentina López', 'valentina@example.com', 'CONFIRMED',
                   19040, 4000, 0, 0
       );

-- Asignar karts
INSERT INTO reserva_karts (reserva_id, kart_id) VALUES
                                                    (9, 'K003'), (9, 'K004'), (9, 'K005'),
                                                    (9, 'K006'), (9, 'K007'), (9, 'K008');

-- Agregar invitados
INSERT INTO reserva_guests (reserva_id, name, email) VALUES
                                                         (9, 'Valentina López', 'valentina@example.com'),
                                                         (9, 'Javier Ruiz', 'javier@example.com'),
                                                         (9, 'Natalia Vega', 'natalia@example.com'),
                                                         (9, 'Gabriel Mora', 'gabriel@example.com'),
                                                         (9, 'Sofía Castro', 'sofia@example.com'),
                                                         (9, 'Daniel Torres', 'daniel@example.com');