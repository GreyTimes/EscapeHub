const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const DEV_USER_ID = import.meta.env.VITE_DEV_USER_ID;

export class ApiError extends Error {

    constructor(message, status, data) {
        super(message);

        this.name = 'ApiError';
        this.status = status;
        this.data = data;
    }
}

export async function apiRequest(path, options = {}) {

    if (!API_BASE_URL) {
        throw new Error(
            'VITE_API_BASE_URL is not configured.'
        );
    }

    const baseUrl = API_BASE_URL.replace(/\/+$/, '');

    const normalizedPath = String(path).replace(/^\/+/, '');

    const url = `${baseUrl}/${normalizedPath}`;

    const {
        body,
        headers: customHeaders,
        ...fetchOptions
    } = options;

    const headers = new Headers(customHeaders ?? {});

    headers.set('Accept', 'application/json');

    // Development user identification.
    // This header is never added to production builds.
    if (import.meta.env.DEV && DEV_USER_ID) {
        headers.set('X-Dev-User-Id', DEV_USER_ID);
    }

    let requestBody = body;

    if (
        body !== undefined &&
        body !== null &&
        typeof body !== 'string' &&
        !(typeof FormData !== 'undefined' && body instanceof FormData)
    ) {
        headers.set('Content-Type', 'application/json');

        requestBody = JSON.stringify(body);
    }

    const response = await fetch(url, {
        ...fetchOptions,
        headers,
        body: requestBody
    });

    let responseData = null;

    if (response.status !== 204) {

        const contentType = response.headers.get('content-type') ?? '';

        if (contentType.includes('application/json')) {
            responseData = await response.json();
        } else {
            responseData = await response.text();
        }
    }

    if (!response.ok) {

        let message = `Request failed with status ${response.status}`;

        if (
            responseData &&
            typeof responseData === 'object' &&
            responseData.message
        ) {
            message = responseData.message;

        } else if (
            typeof responseData === 'string' &&
            responseData.trim()
        ) {
            message = responseData;
        }

        throw new ApiError(
            message,
            response.status,
            responseData
        );
    }

    return responseData;
}
