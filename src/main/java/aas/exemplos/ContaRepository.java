package aas.exemplos;

import java.util.ArrayList;
import java.util.List;

public class ContaRepository {
    static List<Conta> contas;

    public static Conta verificaContas(Long id){
        if(contas== null)
            contas = new ArrayList<>();

        for (Conta c: contas
             ) {
            if(c.getId() == id)
                return c;
        }
        return null;
    }

    public static void adicionaConta(Conta conta){
        contas.add(conta);

    }

}
