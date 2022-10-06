package br.com.sankhya.frescatto.lotes.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.frescatto.lotes.model.Lote;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.Collection;

import static br.com.sankhya.frescatto.lotes.model.Lote.proximoLote;

public class AjusteLotes implements AcaoRotinaJava {

    public static String lote;

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        Registro[] linhas = contextoAcao.getLinhas();
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        //lote = proximoLote();


        for (Registro linha: linhas) {
            BigDecimal nuNota = (BigDecimal) linha.getCampo("NUNOTA");
            CabecalhoNotaVO cabVO = (CabecalhoNotaVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota, CabecalhoNotaVO.class);
            final BigDecimal codTipOper = cabVO.getCODTIPOPER();
            //Collection<DynamicVO> itensNota = cabVO.asCollection("ItemNota");
            Collection<ItemNotaVO> itensNota = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ?", cabVO.getNUNOTA()), ItemNotaVO.class);


            for (ItemNotaVO itemVO: itensNota) {
                lote = proximoLote(itemVO.getCODPROD());
                Lote.geraLotes(itemVO,cabVO);
            }



            //contextoAcao.mostraErro(Lote.proximoLote());



        }


    }
}
