-- für die lokale Entwicklung
-- "ON CONFLICT DO NOTHING": Datensaetze, die es schon gibt, werden beim naechsten Start nicht erneut eingefuegt.

INSERT INTO kategorie (name) VALUES
    ('Bagger'), ('Verdichter'), ('Arbeitsbuehne'), ('Radlader')
ON CONFLICT DO NOTHING;

INSERT INTO standort (name, strasse, plz, ort) VALUES
    ('Bielefeld',  'Industriestrasse 12', '33602', 'Bielefeld'),
    ('Guetersloh', 'Carl-Miele-Strasse 5', '33332', 'Guetersloh')
ON CONFLICT DO NOTHING;

INSERT INTO maschine (inventarnummer, bezeichnung, kategorie_id, standort_id, hersteller, baujahr,
                      betriebsstunden, wartungsintervall_std, tagesmietpreis, garantie_bis)
SELECT v.inventarnummer, v.bezeichnung, k.id, s.id, v.hersteller, v.baujahr,
       v.betriebsstunden, v.wartungsintervall_std, v.tagesmietpreis, v.garantie_bis
FROM (VALUES
    ('M-0001', 'Minibagger CAT 301.7',        'Bagger',        'Bielefeld',  'Caterpillar', 2019, 1250, 500, 89.00,  DATE '2025-06-30'),
    ('M-0002', 'Ruettelplatte BPU 2540',      'Verdichter',    'Bielefeld',  'Wacker Neuson', 2021, 340, 200, 39.00,  DATE '2027-03-31'),
    ('M-0003', 'Scherenbuehne Genie GS-1932', 'Arbeitsbuehne', 'Guetersloh', 'Genie',       2017, 2100, 250, 119.00, NULL)
) AS v(inventarnummer, bezeichnung, kategorie, standort, hersteller, baujahr,
       betriebsstunden, wartungsintervall_std, tagesmietpreis, garantie_bis)
JOIN kategorie k ON k.name = v.kategorie
JOIN standort s ON s.name = v.standort
ON CONFLICT DO NOTHING;

INSERT INTO kunde (kundennummer, firma, vorname, nachname, email, telefon, strasse, plz, ort, ust_id) VALUES
    ('K-1001', 'Mustermann Bau GmbH', 'Max', 'Mustermann', 'max@mustermann-bau.de', '0521 123456',
     'Hauptstrasse 1', '33602', 'Bielefeld', 'DE123456789')
ON CONFLICT DO NOTHING;
