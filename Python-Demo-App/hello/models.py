from hello import db
from flask.ext.login import LoginManager, UserMixin, login_required
from sqlalchemy import CheckConstraint
import base64
import hashlib
import os

try:
    text_type = unicode
except NameError:
    text_type = str

PBKDF2_ITERATIONS = 200000


def _to_bytes(value):
    if value is None:
        return b''
    if isinstance(value, bytes):
        return value
    if isinstance(value, text_type):
        return value.encode('utf-8')
    return str(value).encode('utf-8')


def _constant_time_equals(left, right):
    left = _to_bytes(left)
    right = _to_bytes(right)
    if len(left) != len(right):
        return False
    result = 0
    for a, b in zip(bytearray(left), bytearray(right)):
        result |= a ^ b
    return result == 0


def hash_password(password, salt=None, iterations=PBKDF2_ITERATIONS):
    if salt is None:
        salt = base64.b64encode(os.urandom(16)).decode('ascii')
    digest = hashlib.pbkdf2_hmac(
        'sha256', _to_bytes(password), _to_bytes(salt), iterations)
    encoded = base64.b64encode(digest).decode('ascii')
    return 'pbkdf2_sha256${0}${1}${2}'.format(iterations, salt, encoded)


def verify_password(stored_password, candidate_password):
    try:
        algorithm, iterations, salt, expected = stored_password.split('$', 3)
        if algorithm != 'pbkdf2_sha256':
            return False
        actual = hash_password(candidate_password, salt, int(iterations))
        return _constant_time_equals(stored_password, actual)
    except (AttributeError, TypeError, ValueError):
        return False

class UserProfile(db.Model):
    #defining the UserProfile table to have two columns; comment id and a json string
    is_anonymous = False
    comment_id = db.Column(db.Integer, db.Sequence('id_seq'), primary_key=True)
    doc = db.Column(db.Text, nullable=True)
    __table_args__ =(CheckConstraint('DOC IS JSON', name='ensure_json'), {})

    def __init__(self, text):
        self.doc = text

    @classmethod
    def get(cls, id):
        return UserProfile.query.filter_by(comment_id=id).first()

class User(db.Model):
    #defining the User table to have three columns; user id, username and password hash
    user_id = db.Column(db.Integer, db.Sequence('user_id_seq'), primary_key=True)
    username = db.Column(db.String(50), nullable=False)
    password = db.Column(db.String(255), nullable=False)

    def __init__(self, username, password):
        self.username = username
        self.password = hash_password(password)
        self.is_anonymous = False

    def is_authenticated(self):
        return True

    def is_anonymous(self):
        return False

    def is_active(self):
        return True

    def get_id(self):
        return self.user_id

    def check_password(self, password):
        return verify_password(self.password, password)

    @classmethod
    def get(cls, id):
        return User.query.filter_by(user_id=id).first()

    @classmethod
    def get_by_username(cls, username):
        return User.query.filter_by(username=username).first()
