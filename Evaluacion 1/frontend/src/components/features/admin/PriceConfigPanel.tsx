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
            toast.success(`Configuración de ${formatKey(key)} actualizada correctamente`);
        } catch (error) {
            console.error(`Error updating price for ${key}:`, error);
            toast.error(`Error al actualizar ${formatKey(key)}`);
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

    const configCategories = {
        'Precios Base': [
            'VUELTAS_10_PRECIO',
            'VUELTAS_15_PRECIO',
            'VUELTAS_20_PRECIO'
        ],
        'Descuentos por Grupo (%)': [
            'DESCUENTO_GRUPO_PEQUENO',  // Para 3-5 personas (10%)
            'DESCUENTO_GRUPO_MEDIANO',  // Para 6-10 personas (20%)
            'DESCUENTO_GRUPO_GRANDE'    // Para 11+ personas (30%)
        ],
        'Descuentos por Frecuencia (%)': [
            'DESCUENTO_FRECUENCIA_BAJA',  // 2-4 visitas (10%)
            'DESCUENTO_FRECUENCIA_MEDIA', // 5-6 visitas (20%)
            'DESCUENTO_FRECUENCIA_ALTA'   // 7+ visitas (30%)
        ],
        'Descuento por Cumpleaños (%)': [
            'DESCUENTO_CUMPLEANOS'    // Descuento base por cumpleaños
        ]
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
        <div className="space-y-8">
            {prices && Object.entries(configCategories).map(([category, keys]) => (
                <Card key={category}>
                    <CardHeader>
                        <CardTitle>{category}</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <div className="space-y-4">
                            {keys.map(key => {
                                // Manejar el caso de que algunas claves puedan no existir aún
                                if (prices[key] === undefined) return null;

                                const isPercentage = !category.startsWith('Precios');
                                const inputType = isPercentage ? "number" : "number";
                                const step = isPercentage ? "0.01" : "100";
                                const suffix = isPercentage ? "%" : "";

                                return (
                                    <div key={key} className="grid grid-cols-3 gap-4 items-center">
                                        <Label className="col-span-1">{formatKey(key)}</Label>
                                        <div className="col-span-1 relative">
                                            <Input
                                                type={inputType}
                                                value={prices[key]}
                                                step={step}
                                                min="0"
                                                onChange={(e) => handlePriceChange(key, e.target.value)}
                                                className="pr-6"
                                            />
                                            {suffix && (
                                                <span className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                                                    {suffix}
                                                </span>
                                            )}
                                        </div>
                                        <Button
                                            onClick={() => handleSavePrice(key)}
                                            disabled={isSaving}
                                            className="col-span-1"
                                        >
                                            {isSaving ? 'Guardando...' : 'Guardar'}
                                        </Button>
                                    </div>
                                );
                            })}
                        </div>
                    </CardContent>
                </Card>
            ))}
        </div>
    );
}