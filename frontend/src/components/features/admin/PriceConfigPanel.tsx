import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { getPrices, updateSinglePrice, PriceConfig } from '@/services/adminService';
import { toast } from "sonner";

export default function PriceConfigPanel() {
    const [prices, setPrices] = useState<PriceConfig | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);

    useEffect(() => {
        loadPrices();
    }, []);

    const loadPrices = async () => {
        setIsLoading(true);
        try {
            const data = await getPrices();
            setPrices(data);
        } catch (error) {
            console.error('Error loading prices:', error);
            toast.error("Error al cargar la configuración de precios");
        } finally {
            setIsLoading(false);
        }
    };

    const handlePriceChange = (key: string, value: string) => {
        if (!prices) return;
        setPrices({
            ...prices,
            [key]: parseFloat(value)
        });
    };

    const handleSavePrice = async (key: string) => {
        if (!prices) return;

        setIsSaving(true);
        try {
            await updateSinglePrice(key, prices[key]);
            toast.success(`Precio de ${formatKey(key)} actualizado correctamente`);
        } catch (error) {
            console.error(`Error updating price for ${key}:`, error);
            toast.error(`Error al actualizar el precio de ${formatKey(key)}`);
            // Restaurar el precio anterior
            loadPrices();
        } finally {
            setIsSaving(false);
        }
    };

    const formatKey = (key: string) => {
        return key.replace(/_/g, ' ').toLowerCase()
            .split(' ')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1))
            .join(' ');
    };

    if (isLoading) {
        return (
            <Card>
                <CardContent className="pt-6">
                    <p className="text-center">Cargando configuración de precios...</p>
                </CardContent>
            </Card>
        );
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>Configuración de Precios</CardTitle>
            </CardHeader>
            <CardContent>
                {prices && Object.entries(prices).map(([key, price]) => (
                    <div key={key} className="grid grid-cols-3 gap-4 items-center mb-4">
                        <Label className="col-span-1">{formatKey(key)}</Label>
                        <Input
                            type="number"
                            value={price}
                            onChange={(e) => handlePriceChange(key, e.target.value)}
                            className="col-span-1"
                        />
                        <Button
                            onClick={() => handleSavePrice(key)}
                            disabled={isSaving}
                            className="col-span-1"
                        >
                            {isSaving ? 'Guardando...' : 'Guardar'}
                        </Button>
                    </div>
                ))}
            </CardContent>
        </Card>
    );
}