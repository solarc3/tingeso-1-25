import { PricingResponse } from '@/services/reservationService.ts';

interface PricingSummaryProps {
    pricingDetails: PricingResponse | null;
}

export default function PricingSummary({ pricingDetails }: PricingSummaryProps) {
    if (!pricingDetails) {
        return (
            <div className="text-center p-4">
                <p className="text-muted-foreground">Completa el formulario para ver el precio</p>
            </div>
        );
    }

    const formatCurrency = (value: number) =>
        new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP' }).format(value);

    return (
        <div className="space-y-4">
            <div className="flex justify-between py-2">
                <span>Tarifa Base</span>
                <span className="font-medium">{formatCurrency(pricingDetails.baseRate)}</span>
            </div>

            {pricingDetails.groupDiscount > 0 && (
                <div className="flex justify-between py-2 text-green-600">
                    <span>Descuento Grupal</span>
                    <span>-{formatCurrency(pricingDetails.groupDiscount)}</span>
                </div>
            )}

            {pricingDetails.frequencyDiscount > 0 && (
                <div className="flex justify-between py-2 text-green-600">
                    <span>Descuento Frecuencia</span>
                    <span>-{formatCurrency(pricingDetails.frequencyDiscount)}</span>
                </div>
            )}

            {pricingDetails.birthdayDiscount > 0 && (
                <div className="flex justify-between py-2 text-green-600">
                    <span>Descuento Cumpleaños</span>
                    <span>-{formatCurrency(pricingDetails.birthdayDiscount)}</span>
                </div>
            )}

            <div className="flex justify-between py-2">
                <span>IVA (19%)</span>
                <span>{formatCurrency(pricingDetails.tax)}</span>
            </div>

            <div className="border-t pt-4 mt-2">
                <div className="flex justify-between font-bold text-lg">
                    <span>Total a Pagar</span>
                    <span>{formatCurrency(pricingDetails.totalAmount)}</span>
                </div>
            </div>
        </div>
    );
}