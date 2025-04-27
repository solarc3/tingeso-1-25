import { useState, useEffect } from 'react';

export function TestConnection() {
    const [serverInfo, setServerInfo] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const apiUrl = "/api";

    useEffect(() => {
        fetch(`${apiUrl}/me`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Error: ${response.status}`);
                }
                return response.text();
            })
            .then(data => {
                setServerInfo(data);
                setError(null);
            })
            .catch(err => {
                setError(`Error conectando al backend: ${err.message}`);
                console.error(err);
            });
    }, []);

    return (
        <div className="p-4 border rounded">
            <h2 className="text-lg font-bold mb-2">Prueba de conexión</h2>
            <p className="text-sm mb-2">Conectando a: {apiUrl}</p>
            {error ? (
                <p className="text-red-500">{error}</p>
            ) : serverInfo ? (
                <p className="text-green-500">Conectado correctamente: {serverInfo}</p>
            ) : (
                <p>Cargando...</p>
            )}
        </div>
    );
}