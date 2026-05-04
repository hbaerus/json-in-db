import os


def _required_env(name):
    value = os.environ.get(name)
    if not value:
        raise RuntimeError('%s must be set' % name)
    return value


SECRET_KEY = _required_env('JASON_SECRET_KEY')
# database connection uri
SQLALCHEMY_DATABASE_URI = _required_env('JASON_DATABASE_URI')
SESSION_COOKIE_HTTPONLY = True
SESSION_COOKIE_SAMESITE = 'Lax'
STATIC_ROOT = None

