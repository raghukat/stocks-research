/*   */ package com.stocks.research.mkvapi.exceptions;
/*   */ 
/*   */ public class IonChainAlreadySubscribedException
/*   */   extends Exception {
/*   */   public IonChainAlreadySubscribedException(String chainName) {
/* 6 */     super(String.format("Chain ['%s'] already subscribed", new Object[] { chainName }));
/*   */   }
/*   */ }


/* Location:              \mkvapi\exceptions\IonChainAlreadySubscribedException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */