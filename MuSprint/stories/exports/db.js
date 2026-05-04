// Properties file for the database instance
function requiredEnv(name) {
	const value = process.env[name];
	if (!value) {
		throw new Error(name + ' must be set');
	}
	return value;
}

module.exports = {
			user: requiredEnv('NODE_ORACLEDB_USER'),
			password: requiredEnv('NODE_ORACLEDB_PASSWORD'),
			connectString: requiredEnv('NODE_ORACLEDB_CONNECTIONSTRING')
	};
