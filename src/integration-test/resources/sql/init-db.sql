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
        'iVBORw0KGgoAAAANSUhEUgAAAIsAAACPCAMAAAD9VtjbAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAL3UExURXFxcQICAgAAAAEBAS8vLxERERUVFRQUFBMTExYWFgcHBw4ODsLCwurq6ujo6OPj49ra2tvb2+fn5+np6ebm5tzc3N7e3uXl5eLi4uDg4NnZ2eHh4dbW1t3d3d/f39XV1dfX1+vr61ZWVhAQEP////f39/b29vX19fn5+f7+/vv7+/T09Pz8/PLy8v39/fHx8fPz8/Dw8Pr6+mFhYdTU1O/v7+7u7u3t7ezs7Pj4+FxcXF1dXWNjY2RkZGBgYNPT015eXl9fX+Tk5M3Nzb6+vrW1tby8vMHBwbu7u8PDw8vLy9jY2JKSkmVlZUZGRisrKxgYGAoKCgQEBAMDAwYGBiEhIUFBQVdXV25ubo6OjrKyso+Pj0tLSxwcHBoaGkVFRYuLi9HR0aampk5OTltbW8XFxaOjozg4OA0NDXt7e01NTTs7O5ubm7+/v2dnZw8PD09PTx4eHlNTUzk5OWpqagwMDBcXFyMjIy4uLjc3NzQ0NDAwMCoqKiAgIHh4eGJiYoyMjIiIiK6urtDQ0H19fTIyMgUFBbi4uAsLC729vcjIyBkZGbOzs6qqqpSUlFBQULq6ukRERHNzc6ysrIGBgXZ2dmlpaSQkJFRUVBsbG9nZ2Jycmx8fH6GhoUxMTNLS0jY2NlJSUiYmJp+fn6CgoKSkpFhYWJGRkc7OziIiImhoaEhISFpaWqurq4eHh6ioqMzMzFlZWUZGRa+vr3l5eIqKim1tbXx8fCUlJZycnImJic/Pz29vbxMTFD4+Ppqami0tLaenp0BAQFFRUYaGhsnJySwsLEdHR6WlpT09PaKioklJSYODg8DAwAkJCXl5eZmZmX5+fjExMYSEhJ2dnbe3t42NjVtbWikpKYWFhTo6OkJCQn9/f3p6end3d5aWlrCwsLa2ticnJ3JycpeXl6mpqYCAgJ6enggICMbGxsTExENDQ8rKyjMzM2tra5iYmHV1dRISErm5uZWVlVVVVWZmZpOTkz8/P5CQkEpKSrS0tDw8PAAAAOdW1PAAAAD9dFJOU////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////wD2TzQDAAAACXBIWXMAAA7DAAAOwwHHb6hkAAAM3klEQVR4Xu1by5EjuQ7cmAhdxwPd+jAWyAa5JQN0aDN0kymyQB7oJt0fMvGvIkvduy9i99AcTSEFJEEWCKI+3f3X/dd/pV3+MvBfaD9zGbefuYzbz1zGLeey+7eajV/msrse0PbS5sDlPwArxeFpMyhx2X+gHeRfB0UuTXMg0jUhDaw0L5tAiYvNxRkOIL8FIANALoAEAzK5g7hceUpkONDOFSwVfgJ7KhIg+g14n9ZbNeu57BBWWiEAVFHASlEsCkQuNBjMgcvOHayR5IsRFn0cQG6YmmJMWWsoB/mi55gntAYpF2Bp0oUCMJnALJY0VAz3UVK/BJb+cwqmiVxxIHLRCXMa7iONjAdC/kMqVIsC11DmF1XgIEqFJgWYVD6BSlAGuXvFRM3HJoCcA99Zi04BIDt4U1/wATBZgFHWADIA5ArMZjmsL2hghJfQjIEI919zkcAUWV/WwLmDNRK3YnMGJJHLCYBcA8g3XEhFW/uIJAGaX1AYwIfIABxRQRJlUEKjgPHD9wBB2agvS2oDLhUowvhsafLN/X7lQB3vaTCEAxlONgDkfn89no/arj62Ti+5PnROoYC/EZfYBRpl+eyPp9fjkrdC4mP3eDxfp7NyhyMTuSSY7qPGEqATTIBT5vf96bPOorfd43QF09wEgGOi4nhWX8AxpoHoW8HH4fZpvebtdVSqdRKRWQSZyfO1+gJAGQCWj9vDumy3u7AxcnM3AOP6Yowp4Bld38fE2kO3A3sXL6lQzWAuEhexkWFZwSWHDCA9b/M0WbXd0XqpO/ix5ho4nu9pacoagziJLzWZTOttMgFWcLhGzoCcgC+vjzWu/Dpn6naf1xdQXHaFgMN3pyI5I90xMv05gIgBBnNZ3ddhbILQZK/adpcH2+VSHgGzvWzE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',
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
