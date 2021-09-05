const baseUrl = "http://localhost:8000/api";

/**
 * Authenticate a user using your preferred method.
 * 
 * This method should return an object containing at least
 * the following keys:
 * 
 * { role: "", companyId: "", branchId: "" }
 * 
 * You can add more keys to the object if you need to.
 * 
 * @param {String} username Username of the user
 * @param {String} password Password of the user
 * @returns an object containing at least the following keys:
 *          role, companyId, branchId
 */
async function login(username, password) {
    // TODO: perform login request
    return {
        role: "admin",
        companyId: "1",
        branchId: "1",
    }
}

/**
 * Creates a company.
 * 
 * @param {String} tin Tax Payer Identification number
 * @param {String} name Name of the company
 * @returns A tuple [error, companyId]
 */
async function createCompany(tin, name) {
    try {
        const url = `${baseUrl}/companies`;
        const request = {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ tin: tin, name: name })
        }
        return await executeAndReturnId(url, request);
    } catch (error) {
        return [error, null];
    }
}

/**
 * Creates a new branch of a company.
 * 
 * @param {String} branchName Name of the new branch
 * @param {Int} companyId Parent of the new branch
 * @returns A tuple [error, branchId]
 */
async function createBranch(branchName, companyId) {
    try {
        const url = `${baseUrl}/branches`;
        const request = {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name: branchName, companyId: companyId })
        }
        return await executeAndReturnId(url, request);
    } catch (error) {
        return [error, null];
    }
}

/**
 * Creates a new queue in a branch.
 * 
 * @param {String} queueName Name of the new queue
 * @param {String} initialTurn Initial turn of the new queue: one character, one number. Ex: A1
 * @param {Int} branchId Parent owner of the queue
 * @returns A tuple [error, queueId]
 */
async function createQueue(queueName, initialTurn, branchId) {
    const url = `${baseUrl}/queues`;
    const requestBody = {
        name: queueName,
        initialTurn: initialTurn,
        branchId: branchId
    };

    const request = {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody)
    }

    return await executeAndReturnId(url, request);
}

/**
 * Returns the queues of a branch paginated.
 * 
 * @param {Int} branchId The branch owner of the queues
 * @param {Int} page The page we want to retrieve
 * @returns A tuple [error, [queue]]
 */
async function getQueuesFromBranch(branchId, page) {
    const url = `${baseUrl}/queues?branchId=${branchId}&page=${page}`;

    const request = {
        method: "GET",
        headers: { "Content-Type": "application/json" }
    }

    return await executeAndReturnBody(url, request);
}

/**
 * Calls the next turn in line of the given queue.
 * 
 * @param {Int} queueId the queue owner of the turns
 * @returns A tuple [error, turn]
 */
async function callNextTurn(queueId) {
    const url = `${baseUrl}/turns`;
    const requestBody = { queueId: queueId };

    const request = {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody)
    }

    return await executeAndReturnBody(url, request);
}

/**
 * Request a new turn from a given queue.
 * The owner of the given phone number will receive an SMS
 * saying that a new turn was requested and it is in line.
 * 
 * @param {String} phoneNumber The phone number to send the SMS to.
 * @param {Int} queueId The queue in which we are requesting a new turn.
 * @returns A tuple [error, turn]
 */
async function requestTurn(phoneNumber, queueId) {
    const url = `${baseUrl}/turns`;
    const requestBody = { phoneNumber: phoneNumber, queueId: queueId };

    const request = {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody)
    }

    return await executeAndReturnBody(url, request);
}

/**
 * Marks a turn as started, meaning that the turn is being attended.
 * 
 * @param {Int} turnId The ID of the turn we want to mark as started.
 * @returns A tuple [error, turn]
 */
async function startTurn(turnId) {
    return await changeTurnState(turnId, "started");
}

/**
 * Marks a turn as ended, meaning that the turn was attended.
 * 
 * @param {Int} turnId The ID of the turn we want to mark as ended.
 * @returns A tuple [error, turn]
 */
async function endTurn(turnId) {
    return await changeTurnState(turnId, "ended");
}

//---------------------------------------------------------
// Private functions
//---------------------------------------------------------

/**
 * Executes a HTTP request and returns the response body.
 * This is a private function, do not export it.
 * 
 * @param {String} url The url of the request
 * @param {Object} request An oject with the form: { method: "", headers: {}, body: json }
 * @returns The body of the HTTP response
 */
async function internalExecute(url, request) {
    const response = await fetch(url, request);
    const body = await response.json();
    return body;
}

/**
 * Executes the given request and returns the tuple [error, id].
 * Where id is the identifier of the entity that was created or requested.
 * 
 * @param {*} url The url to send the request to
 * @param {*} request Object containing method, headers and body
 * @returns A tuple [error, id]
 */
async function executeAndReturnId(url, request) {
    const body = await internalExecute(url, request);
    return body.error ? [body.error, null] : [null, body.id];
}

/**
 * Executes the given request and returns the tuple [error, body].
 * Where body is the response body as json.
 * 
 * @param {*} url The url to send the request to
 * @param {*} request Object containing method, headers and body
 * @returns A tuple [error, body]
 */
async function executeAndReturnBody(url, request) {
    const body = await internalExecute(url, request);
    return body.error ? [body.error, null] : [null, body];
}

/**
 * Change the state of a turn to the given state.
 * 
 * @param {*} turnId The ID of the turn we want to modify
 * @param {*} newState The new state we want to asign to a turn
 * @returns A tuple [error, turn]
 */
async function changeTurnState(turnId, newState) {
    const url = `${baseUrl}/turns/${turnId}`;
    const requestBody = { targetState: newState.toUpperCase() };

    const request = {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody)
    }

    return await executeAndReturnBody(url, request);
}

export {
    login, createCompany, createBranch, createQueue, getQueuesFromBranch,
    callNextTurn, requestTurn, startTurn, endTurn
};
