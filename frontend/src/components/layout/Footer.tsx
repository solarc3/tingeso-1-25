export default function Footer() {
    return (
        <footer className="bg-background border-t mt-auto">
            <div className="container mx-auto px-4 py-6">
                <div className="text-center text-sm text-muted-foreground">
                    <p>&copy; {new Date().getFullYear()} KartingRM. Todos los derechos reservados.</p>
                    <p className="mt-2 ">
                        Horarios: Lun-Vie 14:00-22:00 | Sáb-Dom 10:00-22:00
                    </p>
                </div>
            </div>
        </footer>
    );
}