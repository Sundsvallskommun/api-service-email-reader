-- Insert into email
INSERT INTO email (created_at, sender, id, municipality_id, namespace, subject, message)
VALUES (CURRENT_TIMESTAMP,
        'fromaddress@sundsvall.se',
        '81471222-5798-11e9-ae24-57fa13b361e1',
        '2281',
        'myNamespace',
        'Sample subject',
        'Hello, this is a sample email.');

-- Insert into email_entity_to
INSERT INTO email_recipient (email_id, recipients)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'recipient1@sundsvall.se');
INSERT INTO email_recipient (email_id, recipients)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'recipient2@sundsvall.se');

-- Insert into attachment
INSERT INTO attachment (id, created_at, content_type, name, content, email_id)
VALUES (1,
        CURRENT_TIMESTAMP,
        'image/png',
        'test_image.png',
        FROM_BASE64('iVBORw0KGgoAAAANSUhEUgAAAIsAAACPCAMAAAD9VtjbAAAAAXNSR0IArs4c6QAAAARnQU1BAACx jwv8YQUAAAL3UExURXFxcQICAgAAAAEBAS8vLxERERUVFRQUFBMTExYWFgcHBw4ODsLCwurq6ujo 6OPj49ra2tvb2+fn5+np6ebm5tzc3N7e3uXl5eLi4uDg4NnZ2eHh4dbW1t3d3d/f39XV1dfX1+vr 61ZWVhAQEP////f39/b29vX19fn5+f7+/vv7+/T09Pz8/PLy8v39/fHx8fPz8/Dw8Pr6+mFhYdTU 1O/v7+7u7u3t7ezs7Pj4+FxcXF1dXWNjY2RkZGBgYNPT015eXl9fX+Tk5M3Nzb6+vrW1tby8vMHB wbu7u8PDw8vLy9jY2JKSkmVlZUZGRisrKxgYGAoKCgQEBAMDAwYGBiEhIUFBQVdXV25ubo6OjrKy so+Pj0tLSxwcHBoaGkVFRYuLi9HR0aampk5OTltbW8XFxaOjozg4OA0NDXt7e01NTTs7O5ubm7+/ v2dnZw8PD09PTx4eHlNTUzk5OWpqagwMDBcXFyMjIy4uLjc3NzQ0NDAwMCoqKiAgIHh4eGJiYoyM jIiIiK6urtDQ0H19fTIyMgUFBbi4uAsLC729vcjIyBkZGbOzs6qqqpSUlFBQULq6ukRERHNzc6ys rIGBgXZ2dmlpaSQkJFRUVBsbG9nZ2Jycmx8fH6GhoUxMTNLS0jY2NlJSUiYmJp+fn6CgoKSkpFhY WJGRkc7OziIiImhoaEhISFpaWqurq4eHh6ioqMzMzFlZWUZGRa+vr3l5eIqKim1tbXx8fCUlJZyc nImJic/Pz29vbxMTFD4+Ppqami0tLaenp0BAQFFRUYaGhsnJySwsLEdHR6WlpT09PaKioklJSYOD g8DAwAkJCXl5eZmZmX5+fjExMYSEhJ2dnbe3t42NjVtbWikpKYWFhTo6OkJCQn9/f3p6end3d5aW lrCwsLa2ticnJ3JycpeXl6mpqYCAgJ6enggICMbGxsTExENDQ8rKyjMzM2tra5iYmHV1dRISErm5 uZWVlVVVVWZmZpOTkz8/P5CQkEpKSrS0tDw8PAAAAOdW1PAAAAD9dFJOU/////////////////// //////////////////////////////////////////////////////////////////////////// //////////////////////////////////////////////////////////////////////////// //////////////////////////////////////////////////////////////////////////// //////////////////////////////////////////////////////////////////////////// /////////////wD2TzQDAAAACXBIWXMAAA7DAAAOwwHHb6hkAAAM3klEQVR4Xu1by5EjuQ7cmAhd xwPd+jAWyAa5JQN0aDN0kymyQB7oJt0fMvGvIkvduy9i99AcTSEFJEEWCKI+3f3X/dd/pV3+MvBf aD9zGbefuYzbz1zGLeey+7eajV/msrse0PbS5sDlPwArxeFpMyhx2X+gHeRfB0UuTXMg0jUhDaw0 L5tAiYvNxRkOIL8FIANALoAEAzK5g7hceUpkONDOFSwVfgJ7KhIg+g14n9ZbNeu57BBWWiEAVFHA SlEsCkQuNBjMgcvOHayR5IsRFn0cQG6YmmJMWWsoB/mi55gntAYpF2Bp0oUCMJnALJY0VAz3UVK/ BJb+cwqmiVxxIHLRCXMa7iONjAdC/kMqVIsC11DmF1XgIEqFJgWYVD6BSlAGuXvFRM3HJoCcA99Z i04BIDt4U1/wATBZgFHWADIA5ArMZjmsL2hghJfQjIEI919zkcAUWV/WwLmDNRK3YnMGJJHLCYBc A8g3XEhFW/uIJAGaX1AYwIfIABxRQRJlUEKjgPHD9wBB2agvS2oDLhUowvhsafLN/X7lQB3vaTCE AxlONgDkfn89no/arj62Ti+5PnROoYC/EZfYBRpl+eyPp9fjkrdC4mP3eDxfp7NyhyMTuSSY7qPG EqATTIBT5vf96bPOorfd43QF09wEgGOi4nhWX8AxpoHoW8HH4fZpvebtdVSqdRKRWQSZyfO1+gJA GQCWj9vDumy3u7AxcnM3AOP6Yowp4Bld38fE2kO3A3sXL6lQzWAuEhexkWFZwSWHDCA9b/M0WbXd 0XqpO/ix5ho4nu9pacoagziJLzWZTOttMgFWcLhGzoCcgC+vjzWu/Dpn6naf1xdQXHaFgMN3pyI5 I90xMv05gIgBBnNZ3ddhbILQZK/adpcH2+VSHgGzvWzE9BJAz35SX9yaAB8HHyejRru8TkfsLVAg 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'),
        '81471222-5798-11e9-ae24-57fa13b361e1');

INSERT INTO email_metadata (email_id, metadata_key, metadata)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'someKey', 'someValue');

-- Insert into credentials
INSERT INTO credentials (created_at,
                         destination_folder,
                         domain,
                         id,
                         municipality_id,
                         namespace,
                         password,
                         username,
                         action)
VALUES (CURRENT_TIMESTAMP,
        'someDestinationFolder',
        'someDomain',
        '81471222-5798-11e9-ae24-57fa13b361e1',
        '2281',
        'someNamespace',
        'somePassword',
        'someUsername',
        'PERSIST');

-- Insert into credentials_entity_email_address
INSERT INTO credentials_email_address (credentials_id, email_address)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'inbox1@sundsvall.se');
INSERT INTO credentials_email_address (credentials_id, email_address)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'inbox2@sundsvall.se');

INSERT INTO credentials_metadata (credentials_id, metadata_key, metadata)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'someKey', 'someValue');

-- Insert into email_header
INSERT INTO email_header (email_id, id, header_key)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', '81471222-5798-11e9-ae24-57fa13b361e1',
        'REFERENCES');

-- Insert into email_header_value
INSERT INTO email_header_value (header_id, value, order_index)
VALUES ('81471222-5798-11e9-ae24-57fa13b361e1', 'someValue', 0),
       ('81471222-5798-11e9-ae24-57fa13b361e1', 'someOtherValue', 1);

INSERT INTO graph_credentials(created_at, client_id, client_secret, destination_folder, id, municipality_id, namespace,
                              tenant_id)
VALUES ('2019-04-10 12:00:00', 'client_id', 'client_secret', 'destination_folder',
        '81471222-5798-11e9-ae24-57fa13b361e1', '2281', 'namespace',
        'tenant_id');

insert into graph_credentials_email_address(graph_credentials_id, email_address)
values ('81471222-5798-11e9-ae24-57fa13b361e1', 'test@example.com');

insert into graph_credentials_metadata(graph_credentials_id, metadata_key, metadata)
values ('81471222-5798-11e9-ae24-57fa13b361e1', 'metadata_key', 'metadata_value');
