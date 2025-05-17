import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';
import { Server } from 'lucide-react';
export default function Header() {
    const checkLoadBalancer = async () => {
        try {
            const response = await fetch('/api/me');
            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }
            const data = await response.text();
            toast.success(`Servidor respondiendo: ${data}`);
        } catch (error) {
            console.error('Error conectando con el servidor:', error);
            toast.error(`Error conectando con el servidor: ${error instanceof Error ? error.message : 'Desconocido'}`);
        }
    };

    return (
        <header className="bg-background border-b">
            <div className="container mx-auto px-4 py-4 flex items-center justify-between">
                <Link to="/" className="font-bold text-2xl">KartingRM</Link>

                <div className="flex items-center gap-4">
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={checkLoadBalancer}
                        className="flex items-center gap-2"
                    >
                        <Server className="h-4 w-4" />
                        <span>Verificar Servidor</span>
                    </Button>

                    <Link to="/" className="text-foreground hover:text-primary transition-colors">
                        Inicio
                    </Link>
                    <Button asChild>
                        <Link to="/booking">Reservar Ahora</Link>
                    </Button>
                </div>
            </div>
        </header>
    );
}